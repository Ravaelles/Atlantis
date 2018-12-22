package atlantis.strategy;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionManager;
import atlantis.position.APosition;
import atlantis.production.requests.ARequests;
import atlantis.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

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
