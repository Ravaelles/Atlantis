package atlantis.production.dynamic.protoss.units;

import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.Count;

public class ZealotInsteadDragoon {
    public static boolean needZealot() {
        if (!Enemy.zerg()) return false;
//        if (Count.freeGateways() >= 2) return false;

        int zealots = Count.zealots();
        int dragoons = Count.dragoons();

        if (
            dragoons >= 4
                && (zealots == 0 || (zealots <= 0.18 * dragoons))
                && Count.freeGateways() <= 1
                && EnemyUnits.zerglings() >= 8
        ) {
            return true;
        }

        return false;
    }
}