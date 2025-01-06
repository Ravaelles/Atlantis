package atlantis.combat.micro.attack;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class DontAttackUnitScatteredOnMap {
    public static boolean isEnemyScatteredOnMap(AUnit unit, AUnit enemy) {
        if (A.s > 300) return false;
        if (unit.distTo(enemy) <= 1.05) return false;
        if (unit.lastUnderAttackLessThanAgo(60)) return false;
        if (A.isUms()) return false;

        if (dontChaseLonelyZerglings(unit, enemy)) return true;
        if (preventChasingEarlyWorkerScout(unit, enemy)) return false;

        HasPosition squadCenter = unit.squadCenter();
        if (squadCenter != null && squadCenter.distTo(enemy) >= 20) return true;

        if (enemy.isDragoon()) return false;
        if (unit.hasWeaponRangeToAttack(enemy, 0.05)) return false;
        if (enemy.isABuilding()) return false;

        return enemy.enemiesNear().buildings().empty()
            && enemy.friendsNear().buildings().inRadius(8, enemy).empty();
    }

    private static boolean dontChaseLonelyZerglings(AUnit unit, AUnit enemy) {
        return Enemy.zerg()
            && enemy.isZergling()
            && !unit.canAttackTargetWithBonus(enemy, 0)
            && enemy.friendsNear().groundUnits().inRadius(3, unit).atMost(1);
    }

    private static boolean preventChasingEarlyWorkerScout(AUnit unit, AUnit enemy) {
        if (!enemy.isWorker()) return false;
        if (unit.canAttackTargetWithBonus(enemy, 0.1)) return false;

        if (A.s >= 8 * 60) return false;
        if (!unit.isMissionAttackOrGlobalAttack()) return false;

        return enemy.friendsNear().inRadius(2, unit).empty()
            && !unit.canAttackTargetWithBonus(enemy, 0.1)
            && enemy.enemiesNear().buildings().inRadius(6, unit).notEmpty();
    }
}
