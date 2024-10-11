package atlantis.production.dynamic.reinforce.protoss;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class IsProtossBaseSecured {
    private final AUnit base;

    public IsProtossBaseSecured(AUnit base) {
        this.base = base;
    }

    public boolean needsSecuring() {
//        HasPosition nearTo = ABaseLocation.mineralsCenter(base);
        int cannonsNearby = Count.existingOrPlannedBuildingsNear(Protoss_Photon_Cannon, 12, base);

//        System.err.println(base + " cannons = " + cannonsNearby);

        return notEnoughCannons(cannonsNearby);
    }

    private static boolean notEnoughCannons(int cannonsNearby) {
        return cannonsNearby <= minCannons();
    }

    private static int minCannons() {
        return (A.supplyUsed() >= 110 || A.hasMinerals(530)) ? 2 : 1
            + (A.hasMinerals(730) ? 1 : 0);
    }
}
