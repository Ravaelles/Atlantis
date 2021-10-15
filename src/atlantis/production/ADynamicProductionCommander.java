package atlantis.production;


import atlantis.AGame;

public class ADynamicProductionCommander {

    public static void update() {
        if (AGame.isUmsMode()) {
            return;
        }

        ADynamicUnitProductionManager.update();
        ADynamicBuildingsManager.update();
    }
    
}
