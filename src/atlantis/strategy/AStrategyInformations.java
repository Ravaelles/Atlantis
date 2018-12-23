package atlantis.strategy;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AStrategyInformations {
    
    protected static int needDefBuildingAntiLand = 0;

    // === Setters ========================================
    
    public static void needDefBuildingAntiLandAtLeast(int min) {
        if (needDefBuildingAntiLand < min) {
            needDefBuildingAntiLand = min;
        }
    }
    
}
