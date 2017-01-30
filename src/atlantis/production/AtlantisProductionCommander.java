package atlantis.production;

import atlantis.buildings.managers.AtlantisExpansionManager;
import atlantis.buildings.managers.AtlantisSupplyManager;
import atlantis.constructing.AtlantisConstructionManager;

/**
 * Manages construction of new buildings.
 */
public class AtlantisProductionCommander {

    public static void update() {
        
        // Check if need to increase supply and if so, take care of it.
        AtlantisSupplyManager.update();
        
        // See what units/buildings we need to create and take care of it.
        AtlantisProductionManager.update();
        
        // Take care of any unfinished constructions, make sure they have builders assigned etc.
        AtlantisConstructionManager.update();
        
        // Check if we should automatically build new base, because we have shitload of minerals.
        AtlantisExpansionManager.requestNewBaseIfNeeded();
        
        // When it can be applied and makes sense, automatically produce units like workers, factories.
        AtlantisDynamicConstructionCommander.update();
        AtlantisDynamicUnitsCommander.update();
        
    }

}
