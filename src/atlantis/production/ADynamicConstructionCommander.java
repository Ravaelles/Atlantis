package atlantis.production;

import atlantis.AtlantisConfig;
import atlantis.AGame;
import atlantis.constructing.AConstructionManager;
import atlantis.production.orders.ABuildOrdersManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.List;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ADynamicConstructionCommander {

    public static void update() {
        if (AGame.playsAsTerran()) {
            handleBuildFactoriesIfNeeded();
            handleBuildAddonsIfNeeded();
        }
        
        handleBuildGasBuildingsIfNeeded();
    }
    
    // =========================================================

    /**
     * If all factories are busy (training units) request new ones.
     */
    private static void handleBuildFactoriesIfNeeded() {
        if (canAfford(250, 100)) {
            Select<?> factories = Select.ourUnitsOfType(AUnitType.Terran_Factory);
            
            int unfinishedFactories = 
                    AConstructionManager.countNotFinishedConstructionsOfType(AUnitType.Terran_Factory);
            int numberOfFactories = factories.size() + unfinishedFactories;
            
            // Proceed only if all factories are busy
            if (numberOfFactories >= 1 && factories.areAllBusy()) {
                
                if (unfinishedFactories == 0) {
                    AConstructionManager.requestConstructionOf(AUnitType.Terran_Factory);
                }
                else if (unfinishedFactories >= 1 && AGame.canAfford(
                        100 + 200 * unfinishedFactories, 100 + 100 * unfinishedFactories
                )) {
                    AConstructionManager.requestConstructionOf(AUnitType.Terran_Factory);
                }
            }
        }
    }

    /**
     * If there are buildings without addons, build them.
     */
    private static void handleBuildAddonsIfNeeded() {
        if (canAfford(100, 50)) {
            for (AUnit building : Select.ourBuildings().list()) {
                if (building.getType().isFactory() && !building.isBusy() && !building.hasAddon()) {
                    AUnitType addonType = building.getType().getRelatedAddon();
                    if (addonType != null) {
                        building.buildAddon(addonType);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Build Refineries/ Assimilators/ Extractors when it makes sense.
     */
    private static void handleBuildGasBuildingsIfNeeded() {
        if (AGame.getTimeSeconds() % 3 != 0) {
            return;
        }
        
        // =========================================================
        
        int numberOfBases = Select.ourBases().count();
        if (numberOfBases >= 2) {
            int numberOfGasBuildings = Select.ourIncludingUnfinished().ofType(AtlantisConfig.GAS_BUILDING).count();
            if (numberOfBases > numberOfGasBuildings && !AGame.canAfford(0, 350) 
                    && AConstructionManager.countNotStartedConstructionsOfType(AtlantisConfig.GAS_BUILDING) == 0) {
                AConstructionManager.requestConstructionOf(AtlantisConfig.GAS_BUILDING);
            }
        }
    }

    // =========================================================
    
    private static boolean canAfford(int minerals, int gas) {
        return AGame.canAfford(minerals + ABuildOrdersManager.getMineralsNeeded(), gas + ABuildOrdersManager.getGasNeeded()
        );
    }
    
}
