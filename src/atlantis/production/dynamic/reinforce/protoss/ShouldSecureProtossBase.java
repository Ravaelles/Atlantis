package atlantis.production.dynamic.reinforce.protoss;

import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.production.dynamic.protoss.ProtossSecureBasesCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class ShouldSecureProtossBase {
    private final AUnit base;

    public ShouldSecureProtossBase(AUnit base) {
        this.base = base;
    }

    public boolean needsSecuring() {
        if (skipReinforcingMainBase(base)) return false;
        ;

//        HasPosition nearTo = ABaseLocation.mineralsCenter(base);
        int cannonsNearby = Count.existingOrPlannedBuildingsNear(Protoss_Photon_Cannon, 12, base);

//        System.err.println(base + " cannons = " + cannonsNearby);

        return notEnoughCannons(cannonsNearby);
    }

    private static boolean notEnoughCannons(int cannonsNearby) {
        return cannonsNearby < minCannons();
    }

    private static int minCannons() {
        return ((A.supplyTotal() >= 85 || A.hasMinerals(530)) ? 2 : 1)
            + (A.supplyTotal() >= 110 ? 1 : 0)
            + (A.hasMinerals(540) ? 1 : 0)
            + (A.hasMinerals(1240) ? 1 : 0);
    }

    private boolean skipReinforcingMainBase(AUnit base) {
        return base.isMainBase() && consideredMutasAndItsOk();
    }

    private static boolean consideredMutasAndItsOk() {
        if (Enemy.zerg() && A.supplyUsed() >= 110) return false;
//        if (Enemy.zerg() && A.supplyUsed() >= 75 && OurArmy.strength() >= 110) return false;

        return !ProtossSecureBasesCommander.hasMutas();
    }
}
