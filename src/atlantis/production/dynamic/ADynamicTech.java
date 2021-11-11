package atlantis.production.dynamic;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.wrappers.ATech;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ADynamicTech {

    protected static boolean handleUpgrade(TechType tech) {
        if (ATech.isResearched(tech)) {
            return false;
        }

        if (!canAffordWithMargin(tech)) {
            return false;
        }

        AUnit building = Select.ourOfType(AUnitType.create(tech.whatResearches())).free().first();
        if (building != null) {
            return building.research(tech);
        }

        return false;
    }

    protected static boolean handleUpgrade(UpgradeType upgrade) {
        if (ATech.isResearched(upgrade)) {
            return false;
        }

        if (!canAffordWithMargin(upgrade)) {
            return false;
        }

        AUnit building = Select.ourOfType(AUnitType.create(upgrade.whatUpgrades())).free().first();
        if (building != null) {
            return building.upgrade(upgrade);
        }

        return false;
    }

    protected static boolean canAffordWithMargin(TechType tech) {
        int margin = 150;
        return AGame.canAffordWithReserved(tech.mineralPrice() + margin, tech.gasPrice() + margin);
    }

    protected static boolean canAffordWithMargin(UpgradeType upgrade) {
        int margin = 200;
        return AGame.canAffordWithReserved(upgrade.mineralPrice() + margin, upgrade.gasPrice() + margin);
    }

}
