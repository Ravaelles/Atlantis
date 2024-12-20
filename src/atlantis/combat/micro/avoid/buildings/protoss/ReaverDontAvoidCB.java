package atlantis.combat.micro.avoid.buildings.protoss;

import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ReaverDontAvoidCB {
    public static Decision decision(AUnit unit, AUnit combatBuilding) {
        if (dontAvoid(unit, combatBuilding)) {
            return Decision.TRUE;
        }

        return Decision.FALSE;
    }

    private static boolean dontAvoid(AUnit unit, AUnit combatBuilding) {
        double dist = unit.distTo(combatBuilding);

        if (unit.lastAttackFrameMoreThanAgo(30 * 14)) return true;

        if (dist <= 7.4) return false;

//            if (unit.shields() >= 20 && unit.lastAttackFrameMoreThanAgo(30 * 6)) return true;
//
//            if (dist <= 7.25) return false;

        if (unit.shields() >= 40 && unit.lastActionLessThanAgo(50, Actions.ATTACK_UNIT)) return true;

        if (dist <= 7.5) {
            if (unit.isMoving()) {
//                    System.err.println("HoldPosition - " + dist);
                unit.holdPosition("ReaverHoldCB");
            }
            return false;
        }

        if (unit.lastUnderAttackLessThanAgo(70)) return false;

        if (dist >= reaverSafeDist(unit)) return true;
        if (!unit.shotRecently(5)) return true;
        if (!unit.shieldWounded()) return true;

        return false;
    }

    private static double reaverSafeDist(AUnit unit) {
        return 8.0
            + (unit.shieldWounded() ? 0.13 : 0)
            + (unit.hp() <= 90 ? 0.2 : 0);
    }
}
