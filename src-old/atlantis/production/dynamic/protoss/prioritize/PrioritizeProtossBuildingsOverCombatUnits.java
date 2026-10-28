package atlantis.production.dynamic.protoss.prioritize;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.units.select.Count;
import atlantis.util.We;

public class PrioritizeProtossBuildingsOverCombatUnits {
    private static int basesWithUnfinished;

    public static boolean shouldPrioritize() {
        if (!We.protoss()) return false;
        if (A.supplyUsed() >= 70) return false;
        if ((basesWithUnfinished = Count.basesWithUnfinished()) >= 2) return false;

        return againstZerg();
    }

    private static boolean againstZerg() {
        if (!Enemy.zerg()) return false;

        if (basesWithUnfinished <= 1 && EnemyInfo.combatBuildingsAntiLand() >= 2) return !A.hasMinerals(500);

        return false;
    }
}
