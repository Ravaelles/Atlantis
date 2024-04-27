package atlantis.combat.targeting;

import atlantis.units.AUnit;

public class ASquadTargeting {
    public static AUnit useSquadTargetIfPossible(AUnit unit) {
        AUnit enemy = unit.squad().targeting().lastTargetIfAlive();

        if (
            isValidTarget(enemy)
                && unit.distTo(enemy) <= 9
                && unit.hasWeaponRangeToAttack(enemy, unit.isRanged() ? 7 : 4)
                && unit.enemiesNear().canBeAttackedBy(unit, 0).atMost(1)
        ) {
//            if (DEBUG) A.println("SqL enemy = " + enemy.typeWithUnitId());
            return enemy;
        }
        return null;
    }

    private static boolean isValidTarget(AUnit enemy) {
        if (enemy == null) return false;
        if (enemy.isABuilding() && !enemy.isCombatBuilding()) return false;
        if (enemy.isZergling()) return false;
        if (enemy.isWorker()) return false;
        if (enemy.hp() <= 15) return false;

        return true;
    }
}
