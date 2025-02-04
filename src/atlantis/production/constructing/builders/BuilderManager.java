package atlantis.production.constructing.builders;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.game.A;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.ConstructionRequests;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            BuilderAvoidEnemies.class,
            AvoidEnemies.class,
            AbandonAsBuilderIfAttacked.class,
        };
    }

    @Override
    protected Manager handle() {
        if (update()) return usedManager(this);

        return handleSubmanagers();
    }

    private boolean update() {

        // Don't disturb unit that are already constructing
        if (unit.isConstructing() || unit.isMorphing()) return true;

        if (handleConstruction()) return true;

        return false;
    }

    private boolean handleConstruction() {
        Construction construction = ConstructionRequests.constructionFor(unit);
        if (construction != null) {
//            System.err.println("@ " + A.now() + " - construction.status() = " + construction.status());

            // Construction HASN'T STARTED YET, we're probably not even at the required place
            if (construction.status() == ConstructionOrderStatus.NOT_STARTED) {
                AUnit builder = construction.builder();
                if (builder != null && builder.isStuck() && A.now % 96 == 0) {
                    construction.assignOptimalBuilder();
                }

                return (new TravelToConstruct(unit)).travelWhenReady(construction);
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
            return false;
        }
        return false;
    }

    // =========================================================

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
