package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.ProductionOrder;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.build.ZergBuildOrder;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.production.requests.ProduceUnitNow;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;


public class DynamicTrainWorkersCommander extends Commander {
    /**
     * Selects the least worker-saturated base to build a worker.
     */
    @Override
    public void handle() {
        if (!shouldTrainWorkers()) {
            return;
        }

        ProduceUnitNow.produceWorker();
    }

    // =========================================================
    
    public static boolean shouldTrainWorkers() {
        if (AGame.supplyFree() == 0 || !AGame.hasMinerals(50)) {
            return false;
        }

//        if ((A.supplyUsed() <= 154 && !AGame.canAffordWithReserved(50, 0))) {
//            return false;
//        }

        // === Terran ===========================================

        if (We.terran()) {
            ProductionOrder order = ProductionQueue.nextOrderFor(AUnitType.Terran_Comsat_Station, 1);
            if (order != null && order.hasWhatRequired() && !A.hasMinerals(100)) {
                return false;
            }
        }

        // === Zerg ===========================================

        else if (We.zerg()) {
            if (!A.hasMinerals(75 * Count.creepColonies())) {
                return false;
            }

            if (A.supplyUsed() <= 20 && !AGame.canAffordWithReserved(50, 0)) {
                return false;
            }

            ProductionOrder order = ProductionQueue.nextOrderFor(AUnitType.Zerg_Spawning_Pool, 2);
            if (order != null && order.hasWhatRequired() && !A.hasMinerals(250)) {
                return false;
            }

            if (A.supplyUsed() <= 15 && Count.zerglings() < 4) {
                int zerglingsInQueue = ProductionQueue.countInQueue(AUnitType.Zerg_Zergling, 2);
                if (!A.hasMinerals(zerglingsInQueue * 50 + 50)) return false;
            }
        }

        // =========================================================

        // Check if not TOO MANY WORKERS
        int workers = Select.ourWorkers().count();
        if (workers >= (25 * Select.ourBuildingsWithUnfinished().bases().count())) {
            return false;
        }

        // =========================================================
        // Check if AUTO-PRODUCTION of WORKERS is active.

        if (!isAutoProduceWorkersActive(workers)) {
//            System.err.println("AUTO WORKERS DISABLED");
            return false;
        }

        // =========================================================

//        System.err.println("### AUTO-PRODUCE WORKERS ACTIVE");
        return Count.workers() < 60;
    }

    /**
     * Request to produce worker (Zerg Drone, Terran SCV or Protoss Probe) that should be handled according to
     * the race played.
     *
     * See DynamicTrainWorkersCommander which is also used to produce workers.
     */
    public static boolean produceWorker(AUnit base) {
        if (!AGame.canAfford(50, 0) || AGame.supplyFree() == 0) {
            return false;
        }

        if (We.zerg()) {
            return ZergBuildOrder.produceZergUnit(AtlantisConfig.WORKER);
        }

        if (base != null) {
            return base.train(AtlantisConfig.WORKER);
        }

        // If we're here it means all bases are busy. Try queue request
        for (AUnit anotherBase : Select.ourBases().reverse().list()) {
            if (
                    anotherBase.remainingTrainTime() <= 4
                            && anotherBase.hasNothingInQueue()
                            && AGame.supplyFree() >= 2
            ) {
//                System.err.println(
//                    "At supply " + A.supplyUsed() + " produce worker " +
//                        "(" + Count.ourOfTypeWithUnfinished(AtlantisConfig.WORKER) + ")"
//                );

                anotherBase.train(AtlantisConfig.WORKER);
                return true;
            }
        }

        return false;
    }
    
    // =========================================================
        
    public static boolean isAutoProduceWorkersActive(int workers) {
        int autoProduceMinWorkers = BuildOrderSettings.autoProduceWorkersMinWorkers();
        if (A.supplyUsed() < autoProduceMinWorkers) {
            return false;
        }

        int autoProduceMaxWorkers = BuildOrderSettings.autoProduceWorkersMaxWorkers();
        return workers < autoProduceMaxWorkers;
    }

}
