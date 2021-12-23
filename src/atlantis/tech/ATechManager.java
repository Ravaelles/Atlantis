package atlantis.tech;

import atlantis.AGame;
import atlantis.units.AUnitType;
import atlantis.util.Helpers;
import atlantis.wrappers.ATech;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ATechManager extends Helpers {

    public static void researchDynamically() {
        handleResearchAt(30, UpgradeType.Singularity_Charge);

//        buildIfCanAfford(AUnitType.Protoss_Forge);
//
//
//        if (has(AUnitType.Protoss_Forge) && supply(30)) {
//
//        }
    }

    // =========================================================

    private static void handleResearchAt(int minSupply, Object techOrUpgrade) {
        AUnitType required = whatMakes(techOrUpgrade);
        
        if (noSupply(minSupply) || !canAfford(ATech.costOf(techOrUpgrade)) || !hasFree(required)) {
            return;
        }

        ATechRequests.research(techOrUpgrade);
    }

    private static AUnitType whatMakes(Object techUpgradeOrUnit) {
        if (techUpgradeOrUnit instanceof TechType) {
            return AUnitType.create(((TechType) techUpgradeOrUnit).whatResearches());
        } else if (techUpgradeOrUnit instanceof UpgradeType) {
            return AUnitType.create(((UpgradeType) techUpgradeOrUnit).whatUpgrades());
        } else if (techUpgradeOrUnit instanceof AUnitType) {
            return ((AUnitType) techUpgradeOrUnit).whatBuildsIt();
        } else {
            AGame.exit("Neither a tech, nor an upgrade.");
            return null;
        }
    }

}
