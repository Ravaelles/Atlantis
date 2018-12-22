package atlantis.constructing.position;

import atlantis.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranAddonManager {

    public static void buildNewAddon(AUnitType addon, ProductionOrder order) {
        AUnit parentBuilding = Select.ourBuildings().ofType(addon.getWhatBuildsIt()).idle().first();
        if (parentBuilding != null) {
            parentBuilding.buildAddon(addon);
        }
    }
    
}
