package atlantis.production.dynamic;

import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.TechType;
import bwapi.UpgradeType;

public class DynamicTech {
    public static boolean handleResearch(TechType tech) {
        if (ATech.isResearched(tech)) return false;

//        if (!canAffordWithMargin(tech)) {
        if (!A.canAfford(tech)) return false;

        AUnit building = Select.ourOfType(AUnitType.from(tech.whatResearches())).free().first();
        if (building != null) {

            return building.research(tech);
        }

        return false;
    }

    public static boolean handleUpgrade(UpgradeType upgrade) {
        if (ATech.isResearched(upgrade)) return false;

//        if (!canAffordWithMargin(upgrade)) {
        if (!canAfford(upgrade)) return false;

        AUnit building = Select.ourOfType(AUnitType.from(upgrade.whatUpgrades())).free().first();
        if (building != null) {
            return building.upgrade(upgrade);
        }

        return false;
    }

    public static boolean canAfford(TechType tech) {
        return A.canAffordWithReserved(tech.mineralPrice(), tech.gasPrice());
    }

    public static boolean canAffordWithMargin(TechType tech) {
        int margin = 100;
        return A.canAffordWithReserved(tech.mineralPrice() + margin, tech.gasPrice() + margin);
    }

    public static boolean canAfford(UpgradeType upgrade) {
        return A.canAffordWithReserved(upgrade.mineralPrice(), upgrade.gasPrice());
    }

    public static boolean canAffordWithMargin(UpgradeType upgrade) {
        int margin = 150;
        return A.canAffordWithReserved(upgrade.mineralPrice() + margin, upgrade.gasPrice() + margin);
    }

}
