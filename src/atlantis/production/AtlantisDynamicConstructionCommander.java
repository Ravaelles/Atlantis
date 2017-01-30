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
public class AtlantisDynamicConstructionCommander {

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
        if (AtlantisGame.canAfford(140, 60)) {
            Select<?> factories = Select.ourOfType(AUnitType.Terran_Factory);
            
            int unfinishedFactories = 
                    AtlantisConstructionManager.countNotFinishedConstructionsOfType(AUnitType.Terran_Factory);
            int numberOfFactories = factories.size() + unfinishedFactories;
            
            // Proceed only if all factories are busy
            if (numberOfFactories >= 1 && factories.areAllBusy()) {
                
                if (unfinishedFactories == 0) {
                    AtlantisGame.sendMessage("Request factory #JUST_ONE");
                    AtlantisConstructionManager.requestConstructionOf(AUnitType.Terran_Factory);
                }
                else if (unfinishedFactories >= 1 && AtlantisGame.canAfford(
                        100 + 200 * unfinishedFactories, 100 + 100 * unfinishedFactories
                )) {
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
        if (AtlantisGame.canAfford(200, 100)) {
            for (AUnit building : Select.ourBuildings().list()) {
                if (building.getType().isFactory() && !building.isBusy() && !building.hasAddon()) {
                    AUnitType addonType = building.getType().getRelatedAddon();
                    System.out.println(addonType);
                    if (addonType != null) {
                        System.out.println(building + " build " + addonType);
                        building.buildAddon(addonType);
                        return;
                    }
                }
            }
        }
    }
    
}
