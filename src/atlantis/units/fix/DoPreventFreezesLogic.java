package atlantis.units.fix;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.HandleFocusPointPositioning;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class DoPreventFreezesLogic {
    public static boolean handle(Manager parent, AUnit unit) {
//        if (unit.lastUnderAttackLessThanAgo(30 * 3)) {
//            unit.paintCircleFilled(22, Color.Orange);
//        }

//        if (shouldAvoidEnemies(unit)) {
//            if (avoidEnemies(unit)) {
//                unit.setTooltip("Prevent:Avoid");
////                unit.paintCircleFilled(22, Color.Orange);
//                return true;
//            }
//        }

//        if (ToLastSquadTarget.goTo(unit)) {
//            return true;
//        }

//        if ((new HandleFocusPointPositioning(unit)).invokeFrom(parent) != null) {
//            return true;
//        }

//        if ((!unit.isMoving() || unit.looksIdle()) && goToFocus(unit)) {
//            unit.setTooltip("Prevent:Focus");
//            return true;
//        }
//
//        if (goToLeader(unit)) {
//            unit.setTooltip("Prevent:2Leader");
//            return true;
//        }
//
//        if (goToNearestEnemy(unit)) {
//            unit.setTooltip("Prevent:2Enemy");
//            return true;
//        }

//        if (goToNearestCombatFriend(unit)) {
//            return true;
//        }

        if (!unit.isStopped() && unit.lastActionMoreThanAgo(12, Actions.STOP)) {
            unit.stop("Prevent:Stop");
//            unit.setTooltip("Prevent:Stop");
            return true;
        }

//        Manager masnager;
//        if ((manager = new Old_____TooFarFromFocusPoint(unit)).invoke(unit) != null) return true;
//        if ((manager = new Old_____TooCloseToFocusPoint(unit)).invoke(unit) != null) return true;
//
//        unit.paintCircleFilled(22, Color.Yellow);
////        if (goToNearestEnemy(unit)) return true;
//        if (goToCombatUnit(unit)) return true;

        return false;
    }

    private static boolean goToFocus(AUnit unit) {
        AFocusPoint focus = unit.focusPoint();
        if (focus != null && focus.hasPosition()) {
            double distToFocus = unit.distTo(focus);

            if (distToFocus <= 3) {
                return false;
            }
            if (distToFocus >= 6) {
                unit.move(focus, Actions.MOVE_FOCUS, "Prevent:2Focus");
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

    private static boolean goToLeader(AUnit unit) {
        AUnit leader = unit.squadLeader();
        if (leader == null || unit.distTo(leader) <= 5) return false;

        return unit.move(leader, Actions.MOVE_FORMATION, null);
    }

    private static boolean goToCombatUnit(AUnit unit) {
        AUnit nearest = Select.ourCombatUnits().exclude(unit).first();
        if (nearest == null) return false;

        unit.move(nearest, Actions.MOVE_FORMATION, "DoSomething");
//        if (A.seconds() % 2 == 0)
//        else unit.attackUnit(nearest);

        return true;
    }

//    private static boolean shouldAvoidEnemies(AUnit unit) {
//        return unit.cooldown() >= 13
//            || unit.woundPercent() >= 30;
//    }

//    private static boolean avoidEnemies(AUnit unit) {
//        DoAvoidEnemies avoidEnemies = new DoAvoidEnemies(unit);
//
//        return avoidEnemies.handle() != null;
//    }

    private static boolean goToNearestEnemy(AUnit unit) {
        AUnit nearestEnemy = EnemyUnits.discovered().groundUnits().havingPosition().nearestTo(unit);
        if (nearestEnemy == null) return false;

        unit.move(nearestEnemy, Actions.MOVE_ENGAGE, "EngageAnyEnemy");
//        if (A.seconds() % 2 == 0)
//        else unit.attackUnit(nearestEnemy);

        return true;
    }
}
