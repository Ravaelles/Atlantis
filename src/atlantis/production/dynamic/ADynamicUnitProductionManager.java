package atlantis.production.dynamic;

import atlantis.game.AGame;
import atlantis.production.dynamic.protoss.*;
import atlantis.production.dynamic.terran.TerranDynamicBuildingsManager;
import atlantis.production.dynamic.terran.TerranDynamicTech;
import atlantis.production.dynamic.terran.TerranDynamicUnitsManager;
import atlantis.util.We;


public class ADynamicUnitProductionManager {

    public static void update() {
        ADynamicWorkerProductionManager.handleDynamicWorkerProduction();
        
        if (We.terran()) {
            TerranDynamicTech.update();
            TerranDynamicUnitsManager.update();
            TerranDynamicBuildingsManager.update();
        }
        else if (We.protoss()) {
            ProtossDynamicTech.update();
            ProtossDynamicUnitsManager.update();
            ProtossDynamicBuildingsManager.update();
        }
        else if (We.zerg()) {
            ZergDynamicUnitsManager.update();
            ZergDynamicBuildingsManager.update();
        }
    }

}
