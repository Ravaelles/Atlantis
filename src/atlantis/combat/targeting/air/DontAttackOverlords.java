package atlantis.combat.targeting.air;

import atlantis.game.A;
import atlantis.units.AUnit;

public class DontAttackOverlords {
    public static boolean forbidden(AUnit unit) {
        return unit.lastAttackFrameLessThanAgo(30 * 3)
            && unit.enemiesNear().groundUnits().countInRadius(radius(), unit) >= (A.s >= 60 * 8 ? 2 : 1);

//        return unit.enemiesNear().combatUnits().countInRadius(AUnit.NEAR_DIST, unit) <= (unit.shotSecondsAgo() <= 4 ? 1 : 0)
//            && Select.enemyCombatUnits().visibleOnMap().atMost(3);
    }

    private static double radius() {
        return A.s >= 60 * 8 ? 8 : 9.8;
    }
}
