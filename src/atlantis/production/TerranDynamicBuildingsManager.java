package atlantis.production;

import atlantis.AGame;
import atlantis.constructing.AConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;


public class TerranDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
        factoryIfNeeded();
        addonIfNeeded();
    }
    
    // =========================================================
    
    /**
     * If all factories are busy (training units) request new ones.
     */
    private static void factoryIfNeeded() {
        if (canAfford(250, 100)) {
            Select<?> factories = Select.ourOfType(AUnitType.Terran_Factory);
            
            int unfinishedFactories = 
                    AConstructionRequests.countNotFinishedConstructionsOfType(AUnitType.Terran_Factory);
            int numberOfFactories = factories.size() + unfinishedFactories;
            
            // Proceed only if all factories are busy
            if (numberOfFactories >= 1 && factories.areAllBusy()) {
                
                if (unfinishedFactories == 0) {
                    AConstructionRequests.requestConstructionOf(AUnitType.Terran_Factory);
                }
                else if (unfinishedFactories >= 1 && AGame.canAfford(
                        100 + 200 * unfinishedFactories, 100 + 100 * unfinishedFactories
                )) {
                    AConstructionRequests.requestConstructionOf(AUnitType.Terran_Factory);
                }
            }
        }
    }

    /**
     * If there are buildings without addons, build them.
     */
    private static void addonIfNeeded() {
        if (canAfford(100, 50)) {
            for (AUnit building : Select.ourBuildings().list()) {
                if (building.type().isFactory() && !building.isBusy() && !building.hasAddon()) {
                    AUnitType addonType = building.type().getRelatedAddon();
                    if (addonType != null) {
                        building.buildAddon(addonType);
                        return;
                    }
                }
            }
        }
    }

    
}
