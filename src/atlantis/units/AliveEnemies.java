package atlantis.units;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

public class AliveEnemies {
    private static Cache<Selection> cache = new Cache<>();

    public static Selection get() {
//        return enemyUnitsThatWillNotBeDeadNextFrame();

        return cache.get(
            "aliveEnemies",
            0,
            AliveEnemies::enemyUnitsThatWillNotBeDeadNextFrame
        );
    }

    private static Selection enemyUnitsThatWillNotBeDeadNextFrame() {
        return Select.from(
//            EnemyUnits.discovered().list(),
            EnemyUnits.discovered()
                .notDeadMan()
                .list(),
//            EnemyUnits.discovered().list(),
            "enemyUnitsThatWillNotBeDeadNextFrame"
        );
    }
}
