package atlantis.production.dynamic;

import atlantis.game.AGame;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ADynamicTech {

    protected static boolean handleResearch(TechType tech) {
        if (ATech.isResearched(tech)) {
            return false;
        }

//        if (!canAffordWithMargin(tech)) {
        if (!AGame.canAfford(tech)) {
            return false;
        }

        AUnit building = Select.ourOfType(AUnitType.from(tech.whatResearches())).free().first();
        if (building != null) {
//            System.out.println("### Research now " + tech.name());
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

        AUnit building = Select.ourOfType(AUnitType.from(upgrade.whatUpgrades())).free().first();
        if (building != null) {
            return building.upgrade(upgrade);
        }

        return false;
    }

    protected static boolean canAffordWithMargin(TechType tech) {
        int margin = 100;
        return AGame.canAffordWithReserved(tech.mineralPrice() + margin, tech.gasPrice() + margin);
    }

    protected static boolean canAffordWithMargin(UpgradeType upgrade) {
        int margin = 150;
        return AGame.canAffordWithReserved(upgrade.mineralPrice() + margin, upgrade.gasPrice() + margin);
    }

}
