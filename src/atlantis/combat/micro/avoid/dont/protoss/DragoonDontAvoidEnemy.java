package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.combat.retreating.protoss.ProtossTooBigBattleToRetreat;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class DragoonDontAvoidEnemy {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isDragoon()) return false;

        if (Enemy.protoss()) return vsProtoss(unit);

        return false;
    }

    private static boolean vsProtoss(AUnit unit) {
        if (unit.isHealthy() && unit.friendsInRadiusCount(3) > 0) return true;
        if (ProtossTooBigBattleToRetreat.doNotRetreat(unit)) return true;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.4);

        if (meleeEnemiesNearCount > 0 && unit.cooldown() >= 3) return false;

        return unit.hp() > 40
            && (unit.lastAttackFrameAgo() > 30 * 2 || unit.combatEvalRelative() > 1.3)
            && (unit.isHealthy() || meleeEnemiesNearCount == 0);
//            && A.println("Don't avoid " + unit.typeWithUnitId());
    }
}
