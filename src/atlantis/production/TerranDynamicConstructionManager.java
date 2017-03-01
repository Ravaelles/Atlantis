package atlantis.production;

import atlantis.AGame;
import atlantis.constructing.AConstructionManager;
import static atlantis.production.ADynamicConstructionManager.canAfford;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranDynamicConstructionManager extends ADynamicConstructionManager {

    public static void update() {
        requestFactoryIfNeeded();
        requestAddonIfNeeded();
    }
    
    // =========================================================
    
    /**
     * If all factories are busy (training units) request new ones.
     */
    private static void requestFactoryIfNeeded() {
        if (canAfford(250, 100)) {
            Select<?> factories = Select.ourOfType(AUnitType.Terran_Factory);
            
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
    private static void requestAddonIfNeeded() {
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

    
}
