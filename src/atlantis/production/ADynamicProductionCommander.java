package atlantis.production;


import atlantis.AGame;

public class ADynamicProductionCommander {

    public static void update() {
        if (AGame.isUmtMode()) {
            return;
        }

        ADynamicUnitProductionManager.update();
        ADynamicBuildingsManager.update();
    }
    
}
