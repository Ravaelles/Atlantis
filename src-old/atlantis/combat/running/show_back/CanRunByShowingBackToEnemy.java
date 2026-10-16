package atlantis.combat.running.show_back;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class CanRunByShowingBackToEnemy {
    public static boolean check(AUnit unit, AUnit from) {

        //        if (unit.isDragoon() && unit.nearestEnemyDist() <= 3.4) return false;

        if (unit.isFlying()) return true;

        if (true) return false;

        Decision decision;
        if ((decision = forDragoon(unit, from)).notIndifferent()) return decision.toBoolean();
        if ((decision = forWorker(unit, from)).notIndifferent()) return decision.toBoolean();

//        if (!unit.isRunning()) return false;
//        if (unit.friendsInRadiusCount(2) >= 2.5) return false;
        if (unit.enemiesNearInRadius(2.5) >= 2) return false;

//        if (unit.meleeEnemiesNearCount(2.2) >= 2) return true;

//        if (Count.ourCombatUnits() <= 11) return false;
        if (unit.lastActionLessThanAgo(30, Actions.RUN_IN_ANY_DIRECTION)) return false;
        if (unit.hp() <= 18 || unit.lastUnderAttackLessThanAgo(40)) return false;
        if (!unit.isWorker() && !unit.isDragoon()) return false;
//        if (unit.friendsNear().combatUnits().atMost(12)) return false;

        if (unit.meleeEnemiesNearCount(3) >= 4) return true;

        return false;
    }

    private static Decision forWorker(AUnit unit, AUnit from) {
        if (!unit.isWorker()) return Decision.INDIFFERENT;

        if (unit.enemiesNearInRadius(3) == 0) {
            return Decision.TRUE;
        }

        return Decision.FALSE;
    }

    private static Decision forDragoon(AUnit unit, AUnit from) {
        if (!unit.isDragoon()) return Decision.INDIFFERENT;

        if (
            unit.enemiesNearInRadius(A.whenEnemyZerg(3.4, 2.8))
                >= (unit.hp() <= 50 ? 2 : 3)
        ) return Decision.FALSE;

//        if (
//            unit.lastPositionChangedAgo() <= 50
////                && unit.shieldWound() <= 30
//                && unit.shields() >= 22
//                && unit.enemiesNearInRadius(4) <= 2
//        ) return Decision.TRUE;

        return Decision.TRUE;

//        return Decision.INDIFFERENT;
    }
}
