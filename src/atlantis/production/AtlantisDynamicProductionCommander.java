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
            handleBuildAddonsIfNeeded();
        }
    }
    
    // =========================================================

    /**
     * If all factories are busy (training units) request new ones.
     */
    private static void handleBuildFactoriesIfNeeded() {
        if (AtlantisGame.canAfford(180, 80)) {
            Select<?> factories = Select.ourOfType(AUnitType.Terran_Factory);
            
            // Proceed only if all factories are busy
            if (factories.size() >= 1 && factories.areAllBusy()) {
                int numberOfFactoriesInProduction = Select.ourBuildingsIncludingUnfinished()
                        .ofType(AUnitType.Terran_Factory).count();
                
                if (numberOfFactoriesInProduction == 0) {
                    AtlantisGame.sendMessage("Request factory #JUST_ONE");
                    AtlantisConstructionManager.requestConstructionOf(AUnitType.Terran_Factory);
                }
                else if (numberOfFactoriesInProduction >= 1 && AtlantisGame.canAfford(500, 200)) {
                    AtlantisGame.sendMessage("Request factory #MORE");
                    AtlantisConstructionManager.requestConstructionOf(AUnitType.Terran_Factory);
                }
            }
        }
    }

    /**
     * If there are buildings without addons, build them.
     */
    private static void handleBuildAddonsIfNeeded() {
        if (AtlantisGame.canAfford(250, 150)) {
            for (AUnit building : Select.ourBuildings().list()) {
                if (building.canHaveAddon() && !building.hasAddon()) {
                    AUnitType addon = building.getType().getRelatedAddon();
                    if (addon != null) {
                        building.buildAddon(addon);
                        return;
                    }
                }
            }
        }
    }
    
}
