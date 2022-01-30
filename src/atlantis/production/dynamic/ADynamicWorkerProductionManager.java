package atlantis.production.dynamic;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.AProductionManager;
import atlantis.production.ProductionOrder;
import atlantis.production.orders.BuildOrderSettings;
import atlantis.production.orders.CurrentProductionQueue;
import atlantis.production.orders.ProductionQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;


public class ADynamicWorkerProductionManager {

    /**
     * Selects the least worker-saturated base to build a worker.
     */
    public static boolean handleDynamicWorkerProduction() {
        if (!shouldTrainWorkers()) {
            return false;
        }

        return AProductionManager.produceWorker();
    }

    // =========================================================
    
    public static boolean shouldTrainWorkers() {
        if (!AGame.hasMinerals(50)) {
            return false;
        }

        // Check FREE SUPPLY
        if (AGame.supplyFree() == 0 || (Count.workers() < 20 && !AGame.canAffordWithReserved(42, 0))) {
            return false;
        }

        if (We.terran()) {
            ProductionOrder order = ProductionQueue.nextOrderFor(AUnitType.Terran_Comsat_Station, 1);
            if (order != null && order.hasWhatRequired() && !A.hasMinerals(100)) {
                return false;
            }
        }

        // Check if not TOO MANY WORKERS
        int workers = Select.ourWorkers().count();
        if (workers >= (25 * Select.ourBases().count())) {
            return false;
        }

        // =========================================================
        // Check if AUTO-PRODUCTION of WORKERS is active.

        if (!isAutoProduceWorkersActive(workers)) {
//            System.err.println("AUTO WORKERS DISABLED");
            return false;
        }


        // =========================================================

        return Count.workers() < 60;
    }
    
    // =========================================================
        
    public static boolean isAutoProduceWorkersActive(int workers) {
        int autoProduceMinWorkers = BuildOrderSettings.autoProduceWorkersMinWorkers();
        int autoProduceMaxWorkers = BuildOrderSettings.autoProduceWorkersMaxWorkers();

        return autoProduceMinWorkers <= workers && workers < autoProduceMaxWorkers;
    }

}
