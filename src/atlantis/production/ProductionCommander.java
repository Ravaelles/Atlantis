package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.production.constructing.AConstructionManager;
import atlantis.production.dynamic.ADynamicProductionCommander;
import atlantis.units.buildings.ASupplyManager;
import atlantis.util.CodeProfiler;

/**
 * Manages construction of new buildings.
 */
public class ProductionCommander extends Commander {

    /**
     * Produce units and buildings according to build orders.
     */
    public void handler() {
        if (AGame.isUms()) {
            return;
        }

        CodeProfiler.startMeasuring(this);

        // Check if need to increase supply and if so, take care of it.
        ASupplyManager.update();

        // See what units/buildings we need to create and take care of it.
        AProductionManager.update();

        // Take care of any unfinished constructions, make sure they have builders assigned etc.
        AConstructionManager.update();

        // When it can be applied and makes sense, automatically produce units like workers, factories.
        ADynamicProductionCommander.update();

        CodeProfiler.endMeasuring(this);
    }

}
