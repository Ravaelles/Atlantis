package atlantis.combat.micro.avoid.buildings;

import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.We;

public class AvoidCombatBuildingKeepFar {
    private static final double DIST = 25;

    public static boolean shouldKeepFar(AUnit unit) {
        if (!We.protoss()) return false;
        if (unit.isAir()) return false;

        return Count.ourCombatUnits() <= 30
            && Army.strengthWithoutOurCB() <= 600
            && A.supplyUsed() <= 180
            && A.minerals() <= 1500;
    }

    public static double DIST(AUnit unit) {
        return DIST + (unit.isMelee() ? 1.9 : 0);
    }
}
