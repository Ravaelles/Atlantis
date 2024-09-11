package atlantis.combat.micro.generic.unfreezer;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.squad.positioning.protoss.ProtossTooFarFromLeader;
import atlantis.combat.squad.positioning.protoss.ProtossTooLonelyGetCloser;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class UnfreezerShakeUnit {
    public static boolean shake(AUnit unit) {
        if (shouldNotDoAnythingButContinue(unit)) return true;

//        if (!unit.isHoldingPosition() && unit.lastActionMoreThanAgo(6, Actions.HOLD_POSITION)) {
//        if (!unit.isHoldingPosition() && unit.lastActionMoreThanAgo(6, Actions.HOLD_POSITION)) {

//        if (unit.lastActionMoreThanAgo(3, Actions.HOLD_POSITION)) {
//            unit.holdPosition("UnfreezeByHold");
//            return true;
//        }

        if (unit.lastActionMoreThanAgo(11, Actions.STOP)) {
            unit.stop("UnfreezeByStop");
            return true;
        }

        if (unit.enemiesNear().inRadius(2, unit).notEmpty()) {
            if (
                !unit.isAttacking()
                    && unit.lastAttackFrameMoreThanAgo(30 * 2)
                    && unit.lastActionMoreThanAgo(30, Actions.ATTACK_UNIT)
            ) {
                if ((new AttackNearbyEnemies(unit)).handleAttackNearEnemyUnits()) {
                    unit.setTooltip("UnfreezeByAttack");
                    return true;
                }
            }
        }

        if (We.protoss()) {
            if ((new ProtossTooFarFromLeader(unit)).forceHandle() != null) return true;
            if ((new ProtossTooLonelyGetCloser(unit)).forceHandle() != null) return true;
        }

//        if (!unit.isMoving()) {
//            if (goToFocus(unit)) return true;
//        }

//        if (!unit.isStopped() && unit.lastActionMoreThanAgo(20, Actions.STOP)) {
//            unit.stop("UnfreezeByStop");
//            return true;
//        }

//        Selection enemiesNear = unit.enemiesNear().groundUnits().havingPosition();
//        if (enemiesNear.notEmpty()) {
//            AUnit enemy = enemiesNear.nearestTo(unit);
//            if (enemy != null && enemy.distTo(unit) > 5) {
//                if (unit.move(enemy, Actions.MOVE_UNFREEZE, "Unfreeze2Enemy")) return true;
//            }
//        }

//        if (goToLeader(unit)) {
//            unit.setTooltip("UnfreezeByLeader");
//            return true;
//        }

//        if (!unit.isMoving() && goToFocus(unit)) {
//            unit.setTooltip("UnfreezeByFocus");
//            return true;
//        }
//
//        if (goToNearestCombatFriend(unit)) {
//            unit.setTooltip("UnfreezeByFriend");
//            return true;
//        }
//
//        if (unit.isMissionDefendOrSparta() && unit.lastActionMoreThanAgo(30 * 2, Actions.MOVE_UNFREEZE)) {
////            if (unit.lastActionLessThanAgo(30 * 2, Actions.MOVE_UNFREEZE)) return true;
//
//            if (!unit.isMoving()) {
//                HasPosition towards = unit.translateByPixels(
//                    8 - 16 * A.rand(0, 1),
//                    8 - 16 * A.rand(0, 1)
//                );
//
//                if (towards != null && towards.isWalkable()) {
//                    unit.move(towards, Actions.MOVE_UNFREEZE, "UnfreezeByMove");
//                    return true;
//                }
//            }
//
//            if (!unit.isMoving()) {
//                HasPosition focus = unit.focusPoint();
////                AUnit towards = Select.mainOrAnyBuilding();
//
////                if (focus != null && towards != null) focus = focus.translateTilesTowards(2.1, towards);
//
////                if (focus != null && unit.distTo(focus) >= 2) {
//                if (focus != null) {
//                    unit.move(focus, Actions.MOVE_UNFREEZE, "UnfreezeByFocus");
//                    return true;
//                }
//            }
//        }
//
////        if (!unit.isStopped()) {
////            unit.stop("UnfreezeByStop");
////            return true;
////        }
//
//        if (!unit.isMoving()) {
//            if (goToFocus(unit)) return true;
//        }
//
////        if (!unit.isHoldingPosition()) {
////            unit.holdPosition("UnfreezeByHold");
////            return true;
////        }
//
////        if (unit.lastActionMoreThanAgo(10, Actions.HOLD_POSITION)) {
////            unit.holdPosition("Unfreeze!!!");
////            return true;
////        }
//
////        HasPosition goTo = goToPositionNearby(unit);
//////        HasPosition goTo = Select.ourBuildings().random();
//////        HasPosition goTo = unit.enemiesNear().nearestTo(unit);
//////        if (goTo == null && unit.distToLeader() > 3) goTo = unit.squadLeader();
//////        if (goTo == null) goTo = unit.friendsNear().notInRadius(2, unit).nearestTo(unit);
//////            if (goTo == null) goTo = unit.friendsNear().mostDistantTo(unit);
////        if (goTo == null) goTo = Select.our().combatUnits().exclude(unit).nearestTo(unit);
//////        if (goTo == null) goTo = Select.our().exclude(unit).groundUnits().random();
//////        if (goTo == null) goTo = goToPositionNearby(unit);
////
////        if (goTo != null) {
////            unit.moveTactical(goTo, Actions.MOVE_UNFREEZE, "UnfreezeByMove");
////            return true;
////        }
//
//        ErrorLog.printErrorOnce("Unfreezing ERROR " + unit);
        return false;
    }

    private static boolean goToFocus(AUnit unit) {
        AFocusPoint focus = unit.focusPoint();
        if (focus != null) {
            HasPosition goTo = focus;
            if (focus.fromSide() != null) {
                goTo = focus.translateTilesTowards(2.5, focus.fromSide());
            }

            double distToFocus = unit.distTo(focus);

            if (distToFocus <= 3) {
                unit.moveToMain(Actions.MOVE_UNFREEZE, "UnfreezeByMoveBase");
                return true;
            }
            if (distToFocus >= 6) {
                unit.move(goTo, Actions.MOVE_UNFREEZE, "UnfreezeByMove");
                return true;
            }
        }
        return false;
    }

    private static boolean goToLeader(AUnit unit) {
        AUnit leader = unit.squadLeader();

        if (leader != null && leader.distTo(unit) >= 6) {
            if (unit.move(leader, Actions.MOVE_FORMATION, "2Leader")) {
                return true;
            }
        }

        return false;
    }

    private static boolean goToNearestCombatFriend(AUnit unit) {
        AUnit nearest = unit.friendsNear().combatUnits().groundUnits().nearestTo(unit);

        if (nearest != null && nearest.distTo(unit) >= 2) {
            if (unit.move(nearest, Actions.MOVE_FORMATION, "PreventMove2Friend")) {
                return true;
            }
        }

        return false;
    }

    private static APosition goToPositionNearby(AUnit unit) {
        int moduloId = unit.id() % 5;
        return unit.position().translateByPixels(-16 + moduloId * 8, 16 - moduloId * 8);
    }

    private static boolean shouldNotDoAnythingButContinue(AUnit unit) {
        return unit.lastActionLessThanAgo(10, Actions.HOLD_POSITION, Actions.STOP, Actions.MOVE_UNFREEZE);
    }
}
