package atlantis.information.strategy.response.enemy_base;

import atlantis.game.player.Enemy;
import atlantis.units.AUnit;

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
