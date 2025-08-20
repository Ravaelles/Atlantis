package atlantis.information.strategy.response.enemy_base;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class WhenEnemyBaseDiscovered {
    public static void whenBaseDiscovered(AUnit enemyUnit) {
        if (Enemy.zerg()) return;

//        if (
//            A.s <= 700
//                && EnemyUnits.discovered().bases().count() >= 2
//                && Count.basesWithPlanned() <= 1
//        ) AddToQueue.withHighPriority(AtlantisRaceConfig.BASE);
    }
}
