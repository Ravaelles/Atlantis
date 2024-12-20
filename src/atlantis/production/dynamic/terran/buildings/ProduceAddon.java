package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ProduceAddon {
    public static boolean buildNow(AUnitType addonType) {
        AUnitType parentBuilding = addonType.whatBuildsIt();

        assert parentBuilding != null;

        for (AUnit building : Select.ourFree(parentBuilding).list()) {
            if (building != null && !building.hasAddon() && A.canAfford(addonType)) {
                building.buildAddon(addonType);
                return true;
            }
        }

        return false;
    }
}
