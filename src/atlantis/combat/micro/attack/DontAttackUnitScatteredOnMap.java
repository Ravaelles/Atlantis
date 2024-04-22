package atlantis.combat.micro.attack;

import atlantis.game.A;
import atlantis.units.AUnit;

public class DontAttackUnitScatteredOnMap {
    public static boolean isEnemyScatteredOnMap(AUnit unit, AUnit enemy) {
        if (A.s > 300) return false;
        if (A.isUms()) return false;
        if (enemy.isDragoon()) return false;
        if (preventChasingEarlyWorkerScout(unit, enemy)) return false;
        if (unit.hasWeaponRangeToAttack(enemy, 0.05)) return false;
        if (enemy.isABuilding()) return false;

        return enemy.enemiesNear().buildings().empty()
            && enemy.friendsNear().buildings().inRadius(8, enemy).empty();
    }

    private static boolean preventChasingEarlyWorkerScout(AUnit unit, AUnit enemy) {
        return enemy.isWorker()
            && unit.isMelee()
            && A.s <= 250
            && enemy.enemiesNear().buildings().notEmpty();
    }
}
