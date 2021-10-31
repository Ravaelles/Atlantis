package atlantis.production.dynamic;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.production.AProductionManager;
import atlantis.units.select.Count;
import atlantis.units.select.Select;


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

        // Check FREE SUPPLY
        if (AGame.supplyFree() == 0 || !AGame.canAfford(50, 0)) {
            return false;
        }

        // Check if not TOO MANY WORKERS
        int workers = Select.ourWorkers().count();
        if (workers >= (25 * Select.ourBases().count())) {
            return false;
        }

        // =========================================================
        // Check if AUTO-PRODUCTION of WORKERS is active.

        if (!isAutoProduceWorkersActive(workers)) {
            return false;
        }

        // =========================================================

        return Count.workers() < 60;
    }
    
    // =========================================================
        
    public static boolean isAutoProduceWorkersActive(int workers) {
        return workers >= AtlantisConfig.AUTO_PRODUCE_WORKERS_SINCE_N_WORKERS
                && workers < AtlantisConfig.AUTO_PRODUCE_WORKERS_MAX_WORKERS;
    }

}
