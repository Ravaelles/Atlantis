package atlantis.information.strategy.response.enemy_base;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class WhenEnemyBaseDiscovered {
    public static void whenBaseDiscovered(AUnit enemyUnit) {
        if (Enemy.zerg()) return;

        if (A.s <= 700 && Count.basesWithPlanned() <= 1) AddToQueue.withHighPriority(AtlantisRaceConfig.BASE);
    }
}
