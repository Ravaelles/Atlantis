package atlantis.production.dynamic;


import atlantis.game.AGame;

public class ADynamicProductionCommander {

    public static void update() {
        if (AGame.isUms()) {
            return;
        }

        ADynamicUnitProductionManager.update();
        ADynamicBuildingsManager.update();
    }
    
}
