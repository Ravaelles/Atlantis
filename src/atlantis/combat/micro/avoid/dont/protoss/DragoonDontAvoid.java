package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class DragoonDontAvoid {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isDragoon()) return false;

        if (Enemy.protoss()) return vsProtoss(unit);

        System.out.println("Huh????? " + unit.hp() + " / " + Enemy.protoss());

        return false;
    }

    private static boolean vsProtoss(AUnit unit) {
        System.out.println("Well? " + unit);
        return unit.hp() > 40
//            && (unit.lastAttackFrameAgo() > 30 * 2 || unit.combatEvalRelative() > 1.3)
//            && (unit.isHealthy() || unit.meleeEnemiesNearCount(1.6) == 0)
            && A.println("Don't avoid " + unit.typeWithUnitId());
    }
}
