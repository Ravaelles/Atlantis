package atlantis.production.requests.produce;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ProduceAddon {
    public static void produceAddon(AUnitType addon) {
        for (AUnit building : Select.ourOfType(addon.whatBuildsIt()).free().list()) {
            building.buildAddon(addon);
            return;
        }
    }
}