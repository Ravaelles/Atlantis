package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class DragoonDontAvoid {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isDragoon()) return false;

        if (Enemy.protoss()) return vsProtoss(unit);

        return false;
    }

    private static boolean vsProtoss(AUnit unit) {
        return unit.hp() >= 40
            && (unit.lastAttackFrameAgo() > 30 * 2 || unit.combatEvalRelative() > 1.1)
            && unit.meleeEnemiesNearCount(1.6) == 0
            && A.println("Don't avoid " + unit.typeWithUnitId());
    }
}
