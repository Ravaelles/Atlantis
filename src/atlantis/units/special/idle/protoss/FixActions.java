package atlantis.units.special.idle.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class FixActions {
    public static boolean moveToLeader(AUnit unit) {
        HasPosition goTo = unit.squadLeader();
        if (goTo == null) return false;

//        goTo = unit.translateTilesTowards(0.15, goTo);
//        if (goTo == null || !goTo.isWalkable()) return false;

        if (unit.distTo(goTo) >= 2.5 && goTo.isWalkable()) {
            if (unit.move(goTo, Actions.MOVE_IDLE, "FixIdleByLeader")) {
//                System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - " + unit.targetPosition() +
//                    " / " + unit.action());
                return true;
            }
        }
        return false;
    }

    public static boolean attackEnemies(AUnit unit, Manager parent, double evalThreshold) {
        if (unit.eval() < evalThreshold) return false;
        if (!unit.isMissionAttack()) return false;
        if (unit.eval() <= 4 && unit.enemiesThatCanAttackMe(1).notEmpty()) return false;

        if (unit.isRanged() && unit.meleeEnemiesNearCount(1.8) > 0) return false;

        if (unit.enemiesNear().notEmpty()) {
            if (unit.isRanged() && unit.meleeEnemiesNearCount(2.5) > 0) {
                if ((new ProtossAvoidEnemies(unit)).forceHandle() != null) return true;

                return false;
            }

            if ((new AttackNearbyEnemies(unit)).invokedFrom(parent)) return true;
        }

        return false;
    }

    public static boolean movedSlightly(AUnit unit) {
        AFocusPoint focusPoint = unit.focusPoint();
        if (focusPoint == null) return false;

//        if (unit.enemiesThatCanAttackMe(3).empty()) {
//            double distToFocusPoint = unit.distToFocusPoint();
//            if (
//                distToFocusPoint > 5 && unit.move(focusPoint, Actions.MOVE_IDLE)
//            ) {
//                return true;
//            }
//        }

        APosition goTo = unit.position().translateByPixels(2, 2);
        if (goTo != null && goTo.isWalkable()) {
            if (unit.move(goTo, Actions.MOVE_IDLE)) {
                return true;
            }
        }

        return false;
    }
}
