package atlantis.protoss.corsair;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class TransitionToCorsairs {
    public static Decision decision() {
        if (!Enemy.zerg()) return Decision.FORBIDDEN;

        return vsZerg();
    }

    private static Decision vsZerg() {
        if (Count.dragoons() <= 1) return Decision.FORBIDDEN;
        if (Army.strength() <= 115 && Count.ourCombatUnits() <= 5 && Count.cannons() <= 1) return Decision.FORBIDDEN;
        if (A.supplyUsed() <= 40 && EnemyUnits.hydras() >= 10) return Decision.FORBIDDEN;
        if (A.supplyUsed() <= 40) return Decision.FORBIDDEN;

        if (A.supplyUsed() >= 70) return Decision.ALLOWED;
        if (A.supplyUsed() >= 90 && Army.strength() >= 95 && EnemyUnits.lurkers() <= 2) return Decision.ALLOWED;

        Selection enemies = EnemyUnits.discovered();
        return Decision.fromBoolean(enemies.mutalisks().notEmpty());
    }
}
