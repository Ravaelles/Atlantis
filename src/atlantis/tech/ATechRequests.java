package atlantis.tech;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ATechRequests {

    public static boolean research(Object techOrUpgrade) {
        if (techOrUpgrade instanceof TechType) {
            return researchTech((TechType) techOrUpgrade);
        } else if (techOrUpgrade instanceof UpgradeType) {
            return researchUpgrade((UpgradeType) techOrUpgrade);
        } else {
            AGame.exit("Neither a tech, nor an upgrade.");
            return false;
        }
    }

    public static boolean researchTech(TechType tech) {
        AUnitType buildingType = AUnitType.create(tech.whatResearches());
        if (buildingType != null) {
            AUnit building = Select.ourBuildings().ofType(buildingType).free().first();
            if (building != null && !building.isBusy()) {
                building.research(tech);
                return true;
            }
        }
        return false;
    }

    public static boolean researchUpgrade(UpgradeType upgrade) {
        AUnitType buildingType = AUnitType.create(upgrade.whatUpgrades());
        if (buildingType != null) {
            AUnit building = Select.ourBuildings().ofType(buildingType).free().first();
            if (building != null && !building.isBusy()) {
                building.upgrade(upgrade);
                return true;
            }
        }
        return false;
    }

}
