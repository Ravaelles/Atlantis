package atlantis.production;

import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructionManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.List;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisDynamicProductionCommander {

    public static void update() {
        AtlantisWorkerProductionManager.handleWorkerProduction();
        
        if (AtlantisGame.playsAsTerran()) {
            handleBuildFactoriesIfNeeded();
        }
    }
    
    // =========================================================

    private static void handleBuildFactoriesIfNeeded() {
        if (AtlantisGame.canAfford(180, 30)) {
            Select<?> factories = Select.ourOfType(AUnitType.Terran_Factory);
            
            // Proceed only if all factories are busy
            if (factories.size() >= 2 && factories.areAllBusy()) {
                int numberOfFactoriesInProduction = Select.ourBuildingsIncludingUnfinished()
                        .ofType(AUnitType.Terran_Factory).count();
                
                if (numberOfFactoriesInProduction == 0) {
                    AtlantisConstructionManager.requestConstructionOf(AUnitType.Terran_Factory);
                }
                else if (numberOfFactoriesInProduction >= 1 && AtlantisGame.canAfford(500, 200)) {
                    AtlantisConstructionManager.requestConstructionOf(AUnitType.Terran_Factory);
                }
            }
        }
    }
    
}
