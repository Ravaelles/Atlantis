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
                && (zealots == 0 || zealotToDragoonRatioTooLow(zealots, dragoons))
                && Count.freeGateways() <= 1
                && EnemyUnits.zerglings() >= 8
        ) {
            return true;
        }

        return false;
    }

    private static boolean zealotToDragoonRatioTooLow(int zealots, int dragoons) {
        if (calcRatio(EnemyUnits.zerglings(), zealots) <= 3.3) return false;

        return zealots <= (idealZealotToDragoonRatio(zealots, dragoons) * dragoons);
    }

    private static double idealZealotToDragoonRatio(int zealots, int dragoons) {
        if (Enemy.zerg()) return ratioVsZerg(zealots, dragoons);

        return 0.12;
    }

    private static double ratioVsZerg(int zealots, int dragoons) {
        return 0.29;
    }

    private static double calcRatio(int val1, int val2) {
        return val1 / (val2 + 0.1);
    }
}