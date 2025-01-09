package atlantis.combat.micro.avoid.buildings;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.We;

public class AvoidCombatBuildingKeepFar {
    private static final double DIST = 26;

    public static boolean shouldKeepFar() {
        if (!We.protoss()) return false;

        return Count.ourCombatUnits() <= 30
            && A.supplyUsed() <= 180
            && A.minerals() <= 1500;
    }

    public static double DIST(AUnit unit) {
        return DIST + (unit.isMelee() ? 1.9 : 0);
    }
}
