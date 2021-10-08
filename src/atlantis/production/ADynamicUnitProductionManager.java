package atlantis.production;

import atlantis.AGame;


public class ADynamicUnitProductionManager {

    public static void update() {
        ADynamicWorkerProductionManager.handleDynamicWorkerProduction();
        
        if (AGame.isPlayingAsTerran()) {
            TerranDynamicUnitsManager.update();
            TerranDynamicBuildingsManager.update();
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
