package atlantis.production;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.production.orders.AtlantisBuildOrdersManager;
import atlantis.units.AUnit;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisWorkerProductionManager {

    /**
     * Selects the least worker-saturated base to build a worker.
     */
    public static void handleWorkerProduction() {
        
        // Leave minerals for reserved constructions
        if (!AtlantisGame.canAfford(AtlantisBuildOrdersManager.getMineralsNeeded() + 50, 0)) {
            return;
        }
        
        // =========================================================
        
        for (AUnit base : Select.ourBases().reverse().list()) {
            if (!base.isTrainingAnyUnit()) {
                base.train(AtlantisConfig.WORKER);
                return;
            }
        }
    }

    // =========================================================
    
    public static void produceWorker() {
//        AUnit building = Select.ourOneIdle(AtlantisConfig.BASE);
//        if (building != null) {
//            building.train(AtlantisConfig.WORKER);
//        }
        for (AUnit base : Select.ourBases().reverse().list()) {
            if (!base.isTrainingAnyUnit()) {
                base.train(AtlantisConfig.WORKER);
                break;
            }
        }
    }

}
