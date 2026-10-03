package atlantis.information.tech;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ATechManager {

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

//        if (A.supplyUsed() <= minSupply || !canAfford(ATech.costOf(techOrUpgrade)) || !hasFree(required)) {
        if (!canAfford(ATech.costOf(techOrUpgrade)) || !hasFree(required)) {
            return;
        }

        ATechRequests.research(techOrUpgrade);
    }

    /** Was {@code Helpers.canAfford(Integer[])}, which only unwrapped the pair into {@link A#canAfford(int, int)}. */
    private static boolean canAfford(Integer[] mineralsAndGas) {
        return A.canAfford(mineralsAndGas[0], mineralsAndGas[1]);
    }

    /** Was {@code Helpers.hasFree(AUnitType)}, which only wrapped {@link Count#ofTypeFree(AUnitType)}. */
    private static boolean hasFree(AUnitType type) {
        return Count.ofTypeFree(type) > 0;
    }

    private static AUnitType whatMakes(Object techUpgradeOrUnit) {
        if (techUpgradeOrUnit instanceof TechType) {
            return AUnitType.from(((TechType) techUpgradeOrUnit).whatResearches());
        }
        else if (techUpgradeOrUnit instanceof UpgradeType) {
            return AUnitType.from(((UpgradeType) techUpgradeOrUnit).whatUpgrades());
        }
        else if (techUpgradeOrUnit instanceof AUnitType) {
            return ((AUnitType) techUpgradeOrUnit).whatBuildsIt();
        }
        else {
            throw new RuntimeException("Neither a tech, nor an upgrade.");
//            AGame.exit("Neither a tech, nor an upgrade.");
//            return null;
        }
    }

}
