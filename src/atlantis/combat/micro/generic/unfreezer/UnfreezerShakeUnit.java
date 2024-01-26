package atlantis.combat.micro.generic.unfreezer;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class UnfreezerShakeUnit {
    public static boolean shake(AUnit unit) {
        if (shouldNotDoAnythingButContinue(unit)) return true;

        if (unit.isMissionDefendOrSparta() && unit.lastActionMoreThanAgo(30 * 2, Actions.MOVE_UNFREEZE)) {
//            if (unit.lastActionLessThanAgo(30 * 2, Actions.MOVE_UNFREEZE)) return true;

            if (!unit.isMoving()) {
                APosition focus = unit.focusPoint();
                if (focus != null) focus = focus.translateTilesTowards(2.1, Select.mainOrAnyBuilding());
                if (focus != null && unit.distTo(focus) >= 2) {
                    unit.move(focus, Actions.MOVE_UNFREEZE, "UnfreezeByFocus");
                    return true;
                }
            }
        }

        if (!unit.isStopped()) {
            unit.stop("UnfreezeByStop");
            return true;
        }

//        if (!unit.isAttacking() && unit.lastAttackFrameMoreThanAgo(30 * 2)) {
//            if ((new AttackNearbyEnemies(unit)).handleAttackNearEnemyUnits()) {
//                unit.setTooltip("UnfreezeByAttack");
//                return true;
//            }
//        }

        if (!unit.isMoving()) {
            AFocusPoint focus = unit.focusPoint();
            if (focus != null && unit.distTo(focus) >= 2.5) {
                unit.move(focus, Actions.MOVE_UNFREEZE, "UnfreezeByMove");
                return true;
            }
        }

        if (!unit.isHoldingPosition()) {
            unit.holdPosition("UnfreezeByHold");
            return true;
        }

//        if (unit.lastActionMoreThanAgo(10, Actions.HOLD_POSITION)) {
//            unit.holdPosition("Unfreeze!!!");
//            return true;
//        }

//        HasPosition goTo = goToPositionNearby(unit);
////        HasPosition goTo = Select.ourBuildings().random();
////        HasPosition goTo = unit.enemiesNear().nearestTo(unit);
////        if (goTo == null && unit.distToLeader() > 3) goTo = unit.squadLeader();
////        if (goTo == null) goTo = unit.friendsNear().notInRadius(2, unit).nearestTo(unit);
////            if (goTo == null) goTo = unit.friendsNear().mostDistantTo(unit);
//        if (goTo == null) goTo = Select.our().combatUnits().exclude(unit).nearestTo(unit);
////        if (goTo == null) goTo = Select.our().exclude(unit).groundUnits().random();
////        if (goTo == null) goTo = goToPositionNearby(unit);
//
//        if (goTo != null) {
//            unit.moveTactical(goTo, Actions.MOVE_UNFREEZE, "UnfreezeByMove");
//            return true;
//        }

        ErrorLog.printErrorOnce("Unfreezing ERROR " + unit);
        return false;
    }

    private static APosition goToPositionNearby(AUnit unit) {
        int moduloId = unit.id() % 5;
        return unit.position().translateByPixels(-16 + moduloId * 8, 16 - moduloId * 8);
    }

    private static boolean shouldNotDoAnythingButContinue(AUnit unit) {
        return unit.isAccelerating()
            || unit.lastActionLessThanAgo(6, Actions.HOLD_POSITION, Actions.MOVE_UNFREEZE);
    }
}
