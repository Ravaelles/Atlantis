package atlantis.production;

import atlantis.buildings.managers.AtlantisExpansionManager;
import atlantis.buildings.managers.AtlantisSupplyManager;
import atlantis.constructing.AtlantisConstructingManager;

/**
 * Manages construction of new buildings.
 */
public class AtlantisProductionCommander {

    public static void update() {
        
        // Check if need to increase supply and if so, take care of it.
        AtlantisSupplyManager.update();
        
        // See what units/buildings we need to create and take care of it.
        AtlantisProduceUnitManager.update();
        
        // Take care of any unfinished constructions, make sure they have builders assigned etc.
        AtlantisConstructingManager.update();
        
        // Check if we should automatically build new base, because we have shitload of minerals.
        AtlantisExpansionManager.requestNewBaseIfNeeded();
    }

}
