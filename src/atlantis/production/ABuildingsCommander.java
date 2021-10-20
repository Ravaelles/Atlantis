package atlantis.production;

import atlantis.AGame;
import atlantis.buildings.managers.ASupplyManager;
import atlantis.buildings.managers.TerranFlyingBuildingManager;
import atlantis.constructing.AConstructionManager;
import atlantis.repair.ARepairCommander;
import atlantis.util.CodeProfiler;

/**
 * Manages construction of new buildings.
 */
public class ABuildingsCommander {

    /**
     * Produce units and buildings according to build orders.
     */
    public static void update() {
        if (AGame.isUms()) {
            return;
        }

        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_BUILDINGS);
        
        // Check if need to increase supply and if so, take care of it.
        ASupplyManager.update();
        
        // See what units/buildings we need to create and take care of it.
        AProductionManager.update();
        
        // Take care of any unfinished constructions, make sure they have builders assigned etc.
        AConstructionManager.update();
        
        // When it can be applied and makes sense, automatically produce units like workers, factories.
        ADynamicProductionCommander.update();

        // Auxiliary class, avoid using it for anything else than debugging.
        ABuildingManager.update();

        // =========================================================
        
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_BUILDINGS);
    }

}
