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
                && EnemyUnits.zerglings() >= 4
        ) {
            return true;
        }

        return false;
    }

    private static boolean zealotToDragoonRatioTooLow(int zealots, int dragoons) {
        double idealZealotToDragoonRatio = idealZealotToDragoonRatio(zealots, dragoons);
        double currentZealotToDragoonRatio = calcRatio(zealots, dragoons);

        if (Enemy.zerg()) {
            int zerglings = EnemyUnits.zerglings();

            if (dragoons >= 5 && zealots <= 7 && zerglings >= 4) return true;

//            if (currentZealotToDragoonRatio >= 0.22) {
//                double lingToZealotRatio = calcRatio(zerglings, zealots);
//                if (lingToZealotRatio <= 2.7) return false;
//            }
        }

//        if (dragoons >= 6 && lingToZealotRatio >= 4) return true;
//        double ratioDiff = idealZealotToDragoonRatio - currentZealotToDragoonRatio;

        return currentZealotToDragoonRatio < idealZealotToDragoonRatio;
    }

    private static double idealZealotToDragoonRatio(int zealots, int dragoons) {
        if (Enemy.zerg()) return ratioVsZerg(zealots, dragoons);

        return 0.12;
    }

    private static double ratioVsZerg(int zealots, int dragoons) {
        return 0.46;
    }

    private static double calcRatio(int val1, int val2) {
        return val1 / (val2 + 0.1);
    }
}