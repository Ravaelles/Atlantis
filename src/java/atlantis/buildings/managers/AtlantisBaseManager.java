package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.information.AtlantisUnitInformationManager;
import jnibwapi.Unit;

public class AtlantisBaseManager {

    public static void update(Unit base) {

        // Train new workers if allowed
        if (shouldTrainWorkers(base)) {
            base.train(AtlantisConfig.WORKER);
        }
    }

    // =========================================================
    private static boolean shouldTrainWorkers(Unit base) {

        // Check MINERALS
        if (AtlantisGame.getMinerals() < 50) {
            return false;
        }

        // Check FREE SUPPLY
        if (AtlantisGame.getSupplyFree() == 0) {
            return false;
        }

        int workers = AtlantisUnitInformationManager.countOurWorkers();

        // Check if not TOO MANY WORKERS
        if (workers >= 27 * AtlantisUnitInformationManager.countOurBases()) {
            return false;
        }

        // Check if AUTO-PRODUCTION of WORKERS is active.
        if (workers < AtlantisConfig.USE_AUTO_WORKER_PRODUCTION_UNTIL_N_WORKERS) {
            return true;
        }

        // Check if ALLOWED TO PRODUCE IN PRODUCTION QUEUE
//        if (!AtlantisGame.getProductionStrategy().shouldProduceNow(AtlantisConfig.WORKER)) {
//            return false;
//        }
        if (!AtlantisGame.getProductionStrategy().getThingsToProduceRightNow(true).isEmpty()) {
            return false;
        }

        // // Check if not TOO MANY WORKERS
        // if (AtlantisUnitInformationManager.countOurWorkers() >= 27 * AtlantisUnitInformationManager.countOurBases())
        // {
        // return false;
        // }
        return false;
    }

}
