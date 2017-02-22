package atlantis.production;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.TilePosition;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ADynamicUnitsCommander {

    public static void update() {
        AWorkerProductionManager.handleWorkerProduction();
        
        if (AGame.playsAsTerran()) {
            handleFactoryProductionIfNeeded();
        }
    }

    // =========================================================
    
    private static void handleFactoryProductionIfNeeded() {
        for (AUnit factory : Select.ourUnitsOfType(AUnitType.Terran_Factory).listUnits()) {
            if (!factory.isTrainingAnyUnit()) {
                boolean cantAffordTankButCanAffordVulture = AGame.hasMinerals(250)
                        && !AGame.hasGas(70);
                
                if (cantAffordTankButCanAffordVulture) {
                    factory.train(AUnitType.Terran_Vulture);
                }
            }
        }
    }
    
}
