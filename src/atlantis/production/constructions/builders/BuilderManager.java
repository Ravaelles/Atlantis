package atlantis.production.constructions.builders;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class BuilderManager extends Manager {
    public BuilderManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWorker() && unit.isBuilder();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            CancelConstructionsWhenEnemiesNear::new,
            BuilderAvoidEnemies::new,
            ProtossAvoidEnemies::new,
            AbandonAsBuilderIfAttacked::new,
        };
    }

    @Override
    protected Manager handle() {
        // Don't disturb unit that are already constructing.
        if (unit.isConstructing() || unit.isMorphing()) return usedManager(this);

        Construction construction = ConstructionRequests.constructionFor(unit);
        if (construction != null) {
            // Per-second while BUILDER_LOG=1, because the old gate was once per 30 GAME MINUTES
            // (and `printMaxOncePerMinute` on top of that): a 20-minute game produced ONE line,
            // which cannot answer "what did this worker do for the 43 seconds before its
            // construction was cancelled" - the exact question NEXT.md #59 is blocked on. The
            // state that matters is a TRIO over time: the action, the last command and whether
            // the engine says the worker is constructing. Kept behind the env flag so a normal
            // run is silent.
            if (atlantis.config.env.Env.builderLog() && A.everyNthGameFrame(30)) {
                System.err.println("BUILDER_STATE @" + A.now()
                    + " unit=#" + unit.id() + " " + unit.type().name() + " @[" + unit.tx() + "," + unit.ty() + "]"
                    + " construction=" + construction.buildingType().name()
                    + " status=" + construction.status()
                    + " site=[" + (construction.buildPosition() == null ? "null"
                        : construction.buildPosition().tx() + "," + construction.buildPosition().ty()) + "]"
                    + " distToSite=" + (construction.buildPosition() == null ? "-"
                        : A.digit(unit.distTo(construction.buildPosition())))
                    + " groundDistToSite=" + (construction.buildPosition() == null ? "-"
                        : A.digit(unit.groundDist(construction.buildPosition())))
                    + " action=" + unit.action().name()
                    + " lastActionAgo=" + unit.lastActionAgo(unit.action())
                    + " lastCommand=" + unit.lastCommandName()
                    + " lastCommandIssuedAgo=" + unit.lastCommandIssuedAgo()
                    + " isConstructing=" + unit.isConstructing()
                    + " isMoving=" + unit.isMoving()
                    + " canAfford=" + A.canAfford(construction.buildingType().mineralPrice(),
                        construction.buildingType().gasPrice())
                    + " manager=" + unit.manager());
            }
            if (atlantis.config.env.Env.isLocal() && A.now() % (30 * 60) == 0) {
                ErrorLog.printMaxOncePerMinute("Builder diagnostic: unit=" + unit + " construction="
                    + construction.buildingType() + " status=" + construction.status()
                    + " pos=" + construction.buildPosition() + " managerHistory=" + unit.managerLogs());
            }
//            System.err.println("@ " + A.now() + " - construction.status() = " + construction.status());

            // Construction HASN'T STARTED YET, we're probably not even at the required place
            if (construction.status() == ConstructionOrderStatus.NOT_STARTED) {
                AUnit builder = construction.builder();
                if (builder != null && shouldReplaceBuilder(builder) && A.now() % 96 == 0) {
                    construction.assignOptimalBuilder();
                }

                boolean moved = (new TravelToConstruct(unit)).travelWhenReady(construction);
                if (moved) return usedManager(this);

                // The assignment itself owns this worker even on a frame when
                // TravelToConstruct cannot issue a move (e.g. wait-for-resources).
                // Preserve BuilderManager's safety/cancel submanagers, then claim
                // the worker so WorkerDefence/WorkerHelp/Gather cannot hijack it.
                Manager safetyManager = handleSubmanagers();
                return safetyManager != null ? safetyManager : usedManager(this, "BuilderWaiting");
            }
//            else if (construction.status() == ConstructionOrderStatus.IN_PROGRESS) {
//                // Do nothing - construction is pending
//            }
//            else if (construction.status() == ConstructionOrderStatus.FINISHED) {
//                // Do nothing - construction is finished
//            }
        }
        else {
            ErrorLog.printMaxOncePerMinute("construction null for " + unit);
        }

        return handleSubmanagers();
    }

    // =========================================================

    /**
     * True when the assigned builder must be replaced. A stuck builder is the
     * old case; an uncompleted one is the NEXT #63 case: it takes no order at
     * all (canCommand=false), so holding the assignment only delays the build
     * until CancelTooLong kills it. Package-visible as a seam so the rule is
     * assertable without running a game.
     */
    public static boolean shouldReplaceBuilder(AUnit builder) {
        return builder.isStuck() || !builder.isCompleted();
    }

    /**
     * Returns true if given worker has been assigned to construct new building or if the constructions is
     * already in progress.
     */
    public static boolean isBuilder(AUnit worker) {
        if (
            worker.isConstructing()
                || (!We.protoss() && ConstructionRequests.constructionFor(worker) != null)
        ) return true;

        for (Construction construction : ConstructionRequests.constructions) {
            if (worker.equals(construction.builder())) {
                if (construction.buildPosition() == null) return false;

                // A worker assigned to a construction IS a builder, even before it
                // has any order. Measured 2026-10-09 on OpenBW with
                // PRODUCTION_V2=LIVE: the v2 dispatcher created the Construction and
                // assigned this worker, but the worker had no command yet, so
                // `isStopped()` was true, this method answered false,
                // BuilderManager.applies() never ran, TravelToConstruct was never
                // issued - and the same Pylon was re-dispatched every frame forever
                // (NEXT #48). The assignment is the fact; the missing order is what
                // TravelToConstruct exists to fix.
                if (construction.status() == ConstructionOrderStatus.NOT_STARTED) return true;

                if (We.protoss() && !worker.isStopped()) return true;

                // Pending Protoss buildings allow unit to go away
                // Terran and Zerg need to use the worker until construction is finished
                return !We.protoss()
                    || !ConstructionOrderStatus.IN_PROGRESS.equals(construction.status());
            }
        }

        return false;
    }
}
