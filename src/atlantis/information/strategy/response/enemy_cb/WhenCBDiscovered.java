package atlantis.information.strategy.response.enemy_cb;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class WhenCBDiscovered {
    public static void whenCBDiscovered(AUnit enemyUnit) {

        // When enemy goes combat buildings, expand.
        if (
            !Enemy.zerg()
                && A.s <= 700
                && EnemyInfo.combatBuildingsAntiLand() >= 2
                && Count.basesWithPlanned() <= 1
        ) {
            A.println(A.s + "s ----------- Enemy goes combat buildings, expand");
            AddToQueue.withTopPriority(AtlantisRaceConfig.BASE);
        }
    }
}
