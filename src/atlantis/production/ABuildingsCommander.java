package atlantis.production;

import atlantis.game.AGame;
import atlantis.production.constructing.AConstructionManager;
import atlantis.production.dynamic.ADynamicProductionCommander;
import atlantis.terran.TerranFlyingBuildingManager;
import atlantis.units.buildings.ASupplyManager;
import atlantis.util.We;

/**
 * Manages construction of new buildings.
 */
public class ABuildingsCommander {

    /**
     * Produce units and buildings according to build orders.
     */
    public static void update() {

        // Auxiliary class, avoid using it for anything else than debugging.
        ABuildingManager.update();

        if (AGame.isUms()) {
            return;
        }

        // Check if need to increase supply and if so, take care of it.
        ASupplyManager.update();

        // See what units/buildings we need to create and take care of it.
        AProductionManager.update();

        // Take care of any unfinished constructions, make sure they have builders assigned etc.
        AConstructionManager.update();

        // When it can be applied and makes sense, automatically produce units like workers, factories.
        ADynamicProductionCommander.update();

        if (We.terran()) {
            TerranFlyingBuildingManager.update();
        }
    }

}
