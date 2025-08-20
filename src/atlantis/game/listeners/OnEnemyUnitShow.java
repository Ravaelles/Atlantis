package atlantis.game.listeners;

import atlantis.information.enemy.EnemyOnCloseIsland;
import atlantis.units.AUnit;

public class OnEnemyUnitShow {
    public static void update(AUnit unit) {
        if (unit == null) return;

        EnemyOnCloseIsland.potentialEnemyOnIsland(unit);
    }
}
