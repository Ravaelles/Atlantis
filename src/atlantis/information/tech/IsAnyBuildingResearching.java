package atlantis.information.tech;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.TechType;
import bwapi.UpgradeType;

public class IsAnyBuildingResearching {
    public static boolean tech(TechType tech) {
        AUnitType building = AUnitType.from(tech.whatResearches());
        for (AUnit unit : Select.ourOfType(building).list()) {
            if (unit.isResearching() && tech.equals(unit.whatIsResearching())) return true;
        }

        return false;
    }

    public static boolean upgrade(UpgradeType upgrade) {
        AUnitType building = AUnitType.from(upgrade.whatUpgrades());
        for (AUnit unit : Select.ourOfType(building).list()) {
            if (unit.isUpgrading() && upgrade.equals(unit.whatIsUpgrading())) return true;
        }

        return false;
    }
}
