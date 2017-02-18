package atlantis.production;

import atlantis.buildings.managers.AExpansionManager;
import atlantis.buildings.managers.ASupplyManager;
import atlantis.constructing.AtlantisConstructionManager;

/**
 * Manages construction of new buildings.
 */
public class AProductionCommander {

    /**
     * Produce units and buildings according to build orders.
     */
    public static void update() {
        
        // Check if need to increase supply and if so, take care of it.
        ASupplyManager.update();
        
        // See what units/buildings we need to create and take care of it.
        AtlantisProductionManager.update();
        
        // Take care of any unfinished constructions, make sure they have builders assigned etc.
        AtlantisConstructionManager.update();
        
        // Check if we should automatically build new base, because we have shitload of minerals.
        AExpansionManager.requestNewBaseIfNeeded();
        
        // When it can be applied and makes sense, automatically produce units like workers, factories.
        AtlantisDynamicConstructionCommander.update();
        AtlantisDynamicUnitsCommander.update();
        
    }

}
