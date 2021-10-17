package atlantis.production;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.units.AUnit;
import atlantis.units.Count;
import atlantis.units.Select;


public class ADynamicWorkerProductionManager {

    /**
     * Selects the least worker-saturated base to build a worker.
     */
    public static boolean handleDynamicWorkerProduction() {
        
        // Leave minerals for reserved constructions
//        if (!AGame.canAfford(ABuildOrdersManager.getMineralsNeeded() + 50, 0)) {
//            return false;
//        }

        if (!shouldTrainWorkers()) {
            return false;
        }

        // =========================================================

        for (AUnit base : Select.ourBases().reverse().list()) {
            if (!base.isTrainingAnyUnit()) {
                base.train(AtlantisConfig.WORKER);
                return true;
            }
        }
        
        return false;
    }

    // =========================================================
    
    public static boolean shouldTrainWorkers() {

        // Check MINERALS
//        if (AGame.getMinerals() <= 200) {
//            return false;
//        }

        // Check FREE SUPPLY
        if (AGame.getSupplyFree() == 0) {
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

        if (Count.workers() >= 60) {
            return false;
        }

        // Check if ALLOWED TO PRODUCE IN PRODUCTION QUEUE
//        if (!AGame.getBuildOrders().shouldProduceNow(AtlantisConfig.WORKER)) {
//            return false;
//        }
//        if (!AGame.getBuildOrders().getThingsToProduceRightNow(true).isEmpty()) {
//            return false;
//        }
        // // Check if not TOO MANY WORKERS
        // if (AtlantisUnitInformationManager.countOurWorkers() >= 27 * AtlantisUnitInformationManager.countOurBases())
        // {
        // return false;
        // }

        return true;
    }
    
    // =========================================================
        
    public static boolean isAutoProduceWorkersActive(int workers) {
        return workers >= AtlantisConfig.AUTO_PRODUCE_WORKERS_SINCE_N_WORKERS
                && workers < AtlantisConfig.AUTO_PRODUCE_WORKERS_MAX_WORKERS;
    }
    
    public static void produceWorker() {
//        AUnit building = Select.ourOneIdle(AtlantisConfig.BASE);
//        if (building != null) {
//            building.train(AtlantisConfig.WORKER);
//        }
        for (AUnit base : Select.ourBases().reverse().list()) {
            if (!base.isTrainingAnyUnit()) {
                base.train(AtlantisConfig.WORKER);
                return;
            }
        }
    }

}
