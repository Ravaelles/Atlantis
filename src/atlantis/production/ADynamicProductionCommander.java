package atlantis.production;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ADynamicProductionCommander {

    public static void update() {
        ADynamicUnitProductionManager.update();
        ADynamicConstructionManager.update();
    }
    
}
