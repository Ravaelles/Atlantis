package atlantis.production;

import atlantis.AtlantisGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.TilePosition;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisDynamicUnitsCommander {

    public static void update() {
        AtlantisWorkerProductionManager.handleWorkerProduction();
        
        if (AtlantisGame.playsAsTerran()) {
            handleFactoryProductionIfNeeded();
        }
    }

    // =========================================================
    
    private static void handleFactoryProductionIfNeeded() {
        for (AUnit factory : Select.ourUnitsOfType(AUnitType.Terran_Factory).listUnits()) {
            if (!factory.isTrainingAnyUnit()) {
                boolean cantAffordTankButCanAffordVulture = AtlantisGame.hasMinerals(250)
                        && !AtlantisGame.hasGas(70);
                
                if (cantAffordTankButCanAffordVulture) {
                    factory.train(AUnitType.Terran_Vulture);
                }
            }
        }
    }
    
}
