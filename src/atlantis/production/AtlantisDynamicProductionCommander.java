package atlantis.production;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisDynamicProductionCommander {

    public static void update() {
        AtlantisWorkerProductionManager.handleWorkerProduction();
    }
    
}
