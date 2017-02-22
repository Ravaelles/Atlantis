package atlantis.production;

import atlantis.AtlantisConfig;
import atlantis.AGame;
import atlantis.production.orders.ABuildOrdersManager;
import atlantis.units.AUnit;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AWorkerProductionManager {

    /**
     * Selects the least worker-saturated base to build a worker.
     */
    public static void handleWorkerProduction() {
        
        // Leave minerals for reserved constructions
        if (!AGame.canAfford(ABuildOrdersManager.getMineralsNeeded() + 50, 0)) {
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
