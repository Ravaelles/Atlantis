package atlantis.information.tech;

import atlantis.game.AGame;
import atlantis.units.AUnitType;
import atlantis.util.Helpers;
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
            return AUnitType.from(((TechType) techUpgradeOrUnit).whatResearches());
        } else if (techUpgradeOrUnit instanceof UpgradeType) {
            return AUnitType.from(((UpgradeType) techUpgradeOrUnit).whatUpgrades());
        } else if (techUpgradeOrUnit instanceof AUnitType) {
            return ((AUnitType) techUpgradeOrUnit).whatBuildsIt();
        } else {
            throw new RuntimeException("Neither a tech, nor an upgrade.");
//            AGame.exit("Neither a tech, nor an upgrade.");
//            return null;
        }
    }

}
