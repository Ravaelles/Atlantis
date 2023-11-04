package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.SoonInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.production.requests.produce.ProduceWorker;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

import static atlantis.units.AUnitType.Zerg_Spawning_Pool;

public class AutoProduceWorkersCommander extends Commander {
    /**
     * Selects the least worker-saturated base to build a worker.
     */
    @Override
    protected void handle() {
        if (!shouldProduceWorkers()) {
            return;
        }

        ProduceWorker.produceWorker();
    }

    // =========================================================

    public static boolean shouldProduceWorkers() {
        if (
            AGame.supplyFree() == 0
                || !AGame.hasMinerals(50)
                || (A.minerals() < A.reservedMinerals() + 50)
        ) return false;

//        if ((A.supplyUsed() <= 154 && !AGame.canAffordWithReserved(50, 0))) {
//            return false;
//        }

        // === Terran ===========================================

        if (We.terran()) {
            if (
                !A.hasMinerals(100)
                    && SoonInQueue.have(AUnitType.Terran_Comsat_Station, 1)
            ) return false;
        }

        // === Zerg ===========================================

        else if (We.zerg()) {
            if (!A.hasMinerals(75 * Count.creepColonies())) return false;
            if (A.supplyUsed() <= 20 && !AGame.canAffordWithReserved(50, 0)) return false;
            if (!A.hasMinerals(250) && SoonInQueue.have(Zerg_Spawning_Pool, 1)) {
                ProductionOrder order = Queue.get().nextOrders(1).ofType(Zerg_Spawning_Pool).get(0);
                if (order != null && order.supplyRequirementFulfilled()) {
                    return !A.hasMinerals(250);
                }
            }

            if (A.supplyUsed() <= 15 && Count.zerglings() < 4) {
                int zerglingsInQueue = CountInQueue.count(AUnitType.Zerg_Zergling, 2);
                if (!A.hasMinerals(zerglingsInQueue * 50 + 50)) return false;
            }
        }

        // =========================================================

        // Check if not TOO MANY WORKERS
        int workers = Select.ourWorkers().count();
        if (workers >= (25 * Select.ourBuildingsWithUnfinished().bases().count())) return false;

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
     * <p>
     * See AutoProduceWorkersCommander which is also used to produce workers.
     */
    public static boolean produceWorker(AUnit base) {
        if (AGame.supplyFree() == 0 || !AGame.canAfford(50, 0)) return false;
        if (A.supplyUsed() >= 8 && !hasEnoughMineralsToConsiderProducingWorker()) return false;

        if (We.zerg()) return ProduceZergUnit.produceZergUnit(AtlantisRaceConfig.WORKER);
        if (base != null) return base.train(AtlantisRaceConfig.WORKER);

//        if (CountInQueue.count(AtlantisRaceConfig.WORKER) > 0) return false;

        if (queueWorker()) return true;

        return false;
    }

    private static boolean queueWorker() {
        AUnit base = Select.main();

        if (base == null) return false;

        if (base.remainingTrainTime() <= 10 && base.remainingTrainTime() >= 1) {
//                ProductionOrder order = AddToQueue.maxAtATime(AtlantisRaceConfig.WORKER, 1);
            if (base.hasNothingInQueue()) {
                base.train(AtlantisRaceConfig.WORKER);
                return true;
            }
        }

        return false;
    }

    private static boolean hasEnoughMineralsToConsiderProducingWorker() {
        if (ReservedResources.minerals() == 0) return true;

        return A.minerals() >= 200 || (ReservedResources.minerals() - A.minerals()) >= 50;
    }

    // =========================================================

    public static boolean isAutoProduceWorkersActive(int workers) {
        int autoProduceMinWorkers = BuildOrderSettings.autoProduceWorkersMinWorkers();
        if (A.supplyUsed() < autoProduceMinWorkers) return false;

        int autoProduceMaxWorkers = BuildOrderSettings.autoProduceWorkersMaxWorkers();
        return workers < autoProduceMaxWorkers;
    }

}