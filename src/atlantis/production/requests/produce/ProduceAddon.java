package atlantis.production.requests.produce;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ProduceAddon {
    public static boolean produceAddon(AUnitType addon) {
        for (AUnit building : Select.ourOfType(addon.whatBuildsIt()).free().list()) {
            if (building.buildAddon(addon)) return true;
        }

        return false;
    }
}
