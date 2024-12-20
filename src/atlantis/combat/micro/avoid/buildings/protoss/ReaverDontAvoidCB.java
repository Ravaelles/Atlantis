package atlantis.combat.micro.avoid.buildings.protoss;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ReaverDontAvoidCB {

    private static double dist;

    public static Decision decision(AUnit unit, AUnit combatBuilding) {
        if (dontAvoid(unit, combatBuilding)) {
            return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    private static boolean dontAvoid(AUnit unit, AUnit cb) {
        dist = unit.distTo(cb);

        if (unit.hp() >= 100 && unit.lastAttackFrameMoreThanAgo(30 * 14)) return true;
        if (avoidMultipleBuildingsClose(unit, cb)) return false;

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
        if (!unit.shotSecondsAgo(5)) return true;
        if (!unit.shieldWounded()) return true;

        return false;
    }

    private static boolean avoidMultipleBuildingsClose(AUnit unit, AUnit cb) {
        return dist <= 7.55
            && unit.shieldWound() >= 15
            && (A.supplyUsed() <= 195 && !A.canAfford(2000, 400))
            && cb.enemiesNear(4).combatBuildingsAntiLand().atLeast(unit.hp() <= 119 ? 0 : 1);
    }

    private static double reaverSafeDist(AUnit unit) {
        return 8.0
            + (unit.shieldWounded() ? 0.13 : 0)
            + (unit.hp() <= 90 ? 0.2 : 0);
    }
}
