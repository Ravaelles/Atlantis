package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.util.We;

public class ASquadTargeting {
    public static AUnit useSquadTargetIfPossible(AUnit unit) {
        if (unit == null || unit.squad() == null) return null;

        AUnit enemy = unit.squad().targeting().lastTargetIfAlive();

        if (
            isValidTarget(unit, enemy)
                && unit.distTo(enemy) <= 9
                && unit.hasWeaponRangeToAttack(enemy, unit.isRanged() ? 3 : 5)
//                && unit.enemiesNear().canBeAttackedBy(unit, 0).empty()
        ) {
//            if (DEBUG) A.println("SqL enemy = " + enemy.typeWithUnitId());

            return enemy;
        }
        return null;
    }

    private static boolean isValidTarget(AUnit unit, AUnit enemy) {
        if (enemy == null) return false;
        if (enemy.isABuilding() && !enemy.isCombatBuilding()) return false;
        if (enemy.isZergling()) return false;
        if (enemy.isWorker()) return false;
        if (enemy.hp() <= 15) return false;
        if (preventForZealots(unit, enemy)) return false;
        if (!unit.hasWeaponToAttackThisUnit(enemy)) return false;

        return true;
    }

    private static boolean preventForZealots(AUnit unit, AUnit enemy) {
        if (!We.protoss() || !unit.isZealot()) return false;

        if (unit.distTo(enemy) > 1.3) return false;
//        if (preventOvercrowdedZealots(unit, enemy)) return true;
        if (preventZealotsTargetingRanged(unit, enemy)) return true;

        return false;
    }

    private static boolean preventOvercrowdedZealots(AUnit unit, AUnit enemy) {
        return unit.allUnitsNear().inRadius(0.5, unit).atLeast(2);
    }

    private static boolean preventZealotsTargetingRanged(AUnit unit, AUnit enemy) {
        return enemy.isRanged();
    }
}
