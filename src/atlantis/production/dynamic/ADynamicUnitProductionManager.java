package atlantis.production.dynamic;

import atlantis.AGame;
import atlantis.production.dynamic.protoss.ProtossDynamicBuildingsManager;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitsManager;
import atlantis.production.dynamic.protoss.ZergDynamicBuildingsManager;
import atlantis.production.dynamic.protoss.ZergDynamicUnitsManager;
import atlantis.production.dynamic.terran.TerranDynamicBuildingsManager;
import atlantis.production.dynamic.terran.TerranDynamicTech;
import atlantis.production.dynamic.terran.TerranDynamicUnitsManager;


public class ADynamicUnitProductionManager {

    public static void update() {
        ADynamicWorkerProductionManager.handleDynamicWorkerProduction();
        
        if (AGame.isPlayingAsTerran()) {
            TerranDynamicUnitsManager.update();
            TerranDynamicBuildingsManager.update();
            TerranDynamicTech.update();
        }
        else if (AGame.isPlayingAsProtoss()) {
            ProtossDynamicUnitsManager.update();
            ProtossDynamicBuildingsManager.update();
        }
        else if (AGame.isPlayingAsZerg()) {
            ZergDynamicUnitsManager.update();
            ZergDynamicBuildingsManager.update();
        }
    }

}
