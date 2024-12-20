package atlantis.units;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

public class AliveEnemies {
    private static Cache<Object> cache = new Cache<>();

    public static Selection get() {
        return (Selection) cache.get(
            "aliveEnemies",
            0,
            () -> enemyUnitsThatWillNotBeDeadNextFrame()
        );
    }

    private static Selection enemyUnitsThatWillNotBeDeadNextFrame() {
        return Select.from(
//            EnemyUnits.discovered().list(),
            EnemyUnits.discovered().notDeadMan().list(),
//            EnemyUnits.discovered().list(),
            "enemyUnitsThatWillNotBeDeadNextFrame"
        );
    }
}
