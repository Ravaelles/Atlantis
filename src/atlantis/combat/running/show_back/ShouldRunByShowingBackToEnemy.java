package atlantis.combat.running.show_back;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

public class ShouldRunByShowingBackToEnemy {
    public static boolean check(AUnit unit, AUnit from) {
        //        if (unit.isDragoon() && unit.nearestEnemyDist() <= 3.4) return false;

        if (unit.isFlying()) return true;

//        if (!unit.isRunning()) return false;
//        if (unit.friendsInRadiusCount(2) >= 2.5) return false;
        if (unit.enemiesNearInRadius(2.5) >= 2) return false;

        if (unit.isDragoon() && unit.enemiesNearInRadius(4) >= (unit.hp() <= 50 ? 2 : 3)) return false;

//        if (unit.meleeEnemiesNearCount(2.2) >= 2) return true;

//        if (Count.ourCombatUnits() <= 11) return false;
        if (unit.lastActionLessThanAgo(30, Actions.RUN_IN_ANY_DIRECTION)) return false;
        if (unit.hp() <= 18 || unit.lastUnderAttackLessThanAgo(40)) return false;
        if (!unit.isWorker() && !unit.isDragoon()) return false;
//        if (unit.friendsNear().combatUnits().atMost(12)) return false;

        if (unit.meleeEnemiesNearCount(3) >= 4) return true;

        return false;
    }
}
