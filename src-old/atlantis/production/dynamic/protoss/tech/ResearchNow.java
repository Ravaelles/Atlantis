package atlantis.production.dynamic.protoss.tech;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ResearchNow {
    public static boolean research(UpgradeType upgrade) {
        AUnitType buildingType = AUnitType.from(upgrade.whatUpgrades());
        AUnit free = Select.ourFree(buildingType).first();

        if (free != null) return free.upgrade(upgrade);

        return false;
    }

    public static boolean research(TechType tech) {
        AUnitType buildingType = AUnitType.from(tech.whatResearches());
        AUnit free = Select.ourFree(buildingType).first();

        if (free != null) return free.research(tech);

        return false;
    }
}
