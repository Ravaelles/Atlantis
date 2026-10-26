package atlantis.production.constructions.builders;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.battle.avoid.protoss.ProtossAvoidEnemies;
import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

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
                EventLog.key(LogKey.BUILDER_STATE)
                    .param("unit", unit.id())
                    .param("type", unit.type().name())
                    .param("tx", unit.tx())
                    .param("ty", unit.ty())
                    .param("construction", construction.buildingType().name())
                    .param("status", construction.status().name())
                    .param("site", construction.buildPosition() == null ? "-"
                        : construction.buildPosition().tx() + "," + construction.buildPosition().ty())
                    .param("distToSite", construction.buildPosition() == null ? "-"
                        : A.digit(unit.distTo(construction.buildPosition())))
                    .param("groundDistToSite", construction.buildPosition() == null ? "-"
                        : A.digit(unit.groundDist(construction.buildPosition())))
                    .param("action", unit.action().name())
                    .param("lastActionAgo", unit.lastActionAgo(unit.action()))
                    .param("lastCommand", unit.lastCommandName())
                    .param("lastCommandIssuedAgo", unit.lastCommandIssuedAgo())
                    .param("constructing", unit.isConstructing())
                    .param("moving", unit.isMoving())
                    .param("canAfford", A.canAfford(construction.buildingType().mineralPrice(),
                        construction.buildingType().gasPrice()))
                    .param("manager", unit.manager() == null ? "-"
                        : unit.manager().getClass().getSimpleName())
                    .log(A.now(), A.minSec());
            }
//            if (atlantis.config.env.Env.isLocal() && A.now() % (30 * 60) == 0) {
//                ErrorLog.printMaxOncePerMinute("Builder diagnostic: unit=" + unit + " construction="
//                    + construction.buildingType() + " status=" + construction.status()
//                    + " pos=" + construction.buildPosition() + " managerHistory=" + unit.managerLogs());
//            }

            // Construction HASN'T STARTED YET, we're probably not even at the required place
            if (construction.status().countsAsDemand()) {
                AUnit builder = construction.builder();
                if (builder != null && shouldReplaceBuilder(builder) && A.now() % 96 == 0) {
                    construction.assignOptimalBuilder();
                }

                // Standstill decomposition (NEXT #59): when the builder stops moving
                // on a site that never starts, print WHICH canBuild term refuses.
                BuilderDiagnostics.logStandstillBreakdown(unit, construction);

                // NEXT #212: a builder held AT an unstartable site mines
                // instead of standing - the assignment stays (no
                // Builder/Gather tug of war, unlike the IN_PROGRESS
                // alternation), so when the bank covers the price the same
                // travel path claims it back. The forecast is the gate both
                // ways, so there is no new threshold to flap on. Mined
                // FIRST, before travel: the CantAfford tail of
                // IssueBuildOrder returns true (handled), so a branch after
                // travelWhenReady never fires while poor.
                if (shouldMineWhileWaiting(construction, unit)
                    && atlantis.units.workers.gather.AMineralGathering.gatherResources(unit)) {
                    return usedManager(this, "BuilderMiningWhileWaiting");
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
        }
        else {
            ErrorLog.printMaxOncePerMinute("construction null for " + unit);
        }

        return handleSubmanagers();
    }

    // =========================================================

    /**
     * True when the builder waits on the bank, not on its feet: the
     * construction never started, and the same forecast that holds the
     * walk back says the money will not cover it on arrival - so standing
     * at the site earns nothing and mining nearby does. IN_PROGRESS warps
     * never reach here (their hold rule owns them).
     */
    private boolean shouldMineWhileWaiting(Construction construction, AUnit builder) {
        return builder != null && builder.isAlive() && construction.buildPosition() != null
            && construction.status().countsAsDemand() && !builder.isConstructing()
            && ShouldNotTravelToConstructYet.check(construction, builder,
                builder.distTo(construction.buildPosition()));
    }

    /**
     * Does a Protoss builder stay assigned while its construction is
     * IN_PROGRESS?
     *
     * <p>
     * A worker is released to a building that is actually warping - that is
     * the healthy shape (it walks away, the warp continues without it). But an
     * IN_PROGRESS with nothing warping at the site is a refused attempt's
     * order echo, not a warp (measured on Wine: every builder alternating
     * BuilderManager/GatherResources while standing next to its site, nothing
     * ever built): releasing the worker there feeds the alternation, because
     * Gather steals it on every frame it stands still and the build never
     * resumes. Hold it instead - demote or cancel will resolve the status,
     * and both need the worker still assigned.
     * </p>
     */
    public static boolean holdInProgressBuilder(boolean stopped, boolean warpingNearby) {
        if (!stopped) return true;

        return !warpingNearby;
    }

    private static boolean isAnyBuildingWarpingAtSite(Construction construction) {
        if (construction.buildPosition() == null) return false;

        return Select.ourUnfinished().ofType(construction.buildingType())
            .inRadius(2.5, construction.buildPosition()).notEmpty();
    }

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

                // Pending Protoss buildings allow unit to go away - but only to
                // a building that is actually warping (see holdInProgressBuilder).
                // Terran and Zerg need to use the worker until construction is finished.
                if (!We.protoss()) return true;

                if (ConstructionOrderStatus.IN_PROGRESS.equals(construction.status())) {
                    return holdInProgressBuilder(worker.isStopped(), isAnyBuildingWarpingAtSite(construction));
                }

                return true;
            }
        }

        return false;
    }
}
