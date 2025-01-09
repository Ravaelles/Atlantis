package atlantis.combat.retreating.protoss.big_scale;

import atlantis.game.player.Enemy;
import atlantis.units.AUnit;

public class ProtossApprxRetreat {
    public static boolean check(AUnit unit) {
        if (Enemy.protoss()) return vsProtoss(unit);
        if (Enemy.zerg()) return vsZerg(unit);

        return false;
    }

    // =========================================================

    private static boolean vsProtoss(AUnit unit) {
        int radius = 7;
        return ourRangedInRadius(unit, radius) < enemyRangedInRadius(unit, radius);
    }

    private static boolean vsZerg(AUnit unit) {
        int radius = 6;
        return ourRangedInRadius(unit, radius) * 1.7 < enemyRangedInRadius(unit, radius);
    }

    // =========================================================

    private static int enemyRangedInRadius(AUnit unit, int radius) {
        return unit.enemiesNear().ranged().countInRadius(radius, unit);
    }

    private static int ourRangedInRadius(AUnit unit, int radius) {
        return unit.friendsNear().ranged().countInRadius(radius, unit) + 1;
    }
}
