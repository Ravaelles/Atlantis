package atlantis.production;

import atlantis.buildings.managers.AExpansionManager;
import atlantis.buildings.managers.ASupplyManager;
import atlantis.constructing.AConstructionManager;
import atlantis.util.CodeProfiler;

/**
 * Manages construction of new buildings.
 */
public class AProductionCommander {

    /**
     * Produce units and buildings according to build orders.
     */
    public static void update() {
        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_PRODUCTION);
        
        // Check if need to increase supply and if so, take care of it.
        ASupplyManager.update();
        
        // See what units/buildings we need to create and take care of it.
        AProductionManager.update();
        
        // When it can be applied and makes sense, automatically produce units like workers, factories.
        ADynamicConstructionCommander.update();
        ADynamicUnitsCommander.update();
        
        
        // Take care of any unfinished constructions, make sure they have builders assigned etc.
        AConstructionManager.update();
        
        // Check if we should automatically build new base, because we have shitload of minerals.
        AExpansionManager.requestNewBaseIfNeeded();
        
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_PRODUCTION);
    }

}
