package atlantis.production;


public class ADynamicProductionCommander {

    public static void update() {
        ADynamicUnitProductionManager.update();
        ADynamicConstructionManager.update();
    }
    
}
