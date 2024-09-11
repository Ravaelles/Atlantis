package atlantis.protoss;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class ProtossMissionAdjustments {

    public static boolean allowsToAttackEnemyUnits(AUnit unit, AUnit enemy) {
        if (unit.isZealot() && (enemy.isZealot() || enemy.isZergling())) {
            Selection cannonsNearby = unit.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon);

            if (cannonsNearby.inRadius(2.2, unit).notEmpty()) {
                return true;
            }

//            if (GamePhase.isEarlyGame() && Count.ourCombatUnits() <= 7) {
//
//            }

//            int ourZealots = unit.friendlyZealotsNearCount(1.3);
            int ourZealots = enemy.enemyZealotsNearCount(1.1);
            if (ourZealots < unit.enemiesNear().inRadius(1.2, unit).count()) return false;
        }

        return false;
    }

}
