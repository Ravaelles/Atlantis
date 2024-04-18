package atlantis.combat.micro.attack;

import atlantis.game.A;
import atlantis.units.AUnit;

public class DontAttackUnitScatteredOnMap {
    public static boolean isEnemyScatteredOnMap(AUnit unit, AUnit enemy) {
        if (A.s > 600) return false;
        if (A.isUms()) return false;
        if (unit.hasWeaponRangeToAttack(enemy, 0.05)) return false;

        return enemy.enemiesNear().buildings().empty()
            && enemy.friendsNear().buildings().inRadius(8, enemy).empty();
    }
}
