package atlantis.information.decisions.protoss.dragoon;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class DragoonInsteadZealotVsZerg {
    public static boolean dragoonInsteadOfZealot_vZ() {
        if (!Enemy.zerg()) return false;

        if (A.gas() >= 100) return true;

        if (transitionToDragoonsDueToMutas()) return true;
        if (notEnoughZealots()) return false;
        if (enoughZealots()) return false;

//        if (A.minerals() >= 170 && Count.gateways() >= 3) return true;
//        if (A.hasGas(50) && !A.hasMinerals(225) && Count.dragoons() <= 2 && Count.zealots() >= 1) return true;

        return false;
    }

    private static boolean enoughZealots() {
        return (OurArmy.strength() >= 115 || Count.zealots() >= 8)
            && Count.zealots() >= minZealots();
    }

    private static boolean notEnoughZealots() {
        if (A.s >= 350 && OurArmy.strength() >= 90 && EnemyInfo.enemyUnitInMainBase() == null) return false;

        if (Count.zealots() <= minZealots()) return true;

        if (
            Enemy.zerg()
                && OurArmy.strength() <= 95
                && A.seconds() <= 420
//                && Count.zealots() <= Math.max(4, EnemyUnits.discovered().zealots().count() * 0.3)
//                && Count.zealots() <= Math.max(4, EnemyUnits.discovered().zealots().count() * 0.3)
                && Count.zealots() <= 3
                && EnemyUnits.discovered().zerglings().atLeast(8)
        ) {
            return true;
        }

//        if (
//            A.seconds() <= 400
//                && Count.zealots() <= minZealots()
////                && A.seconds() <= 400
////                && Count.zealots() <= minZealots()
//        ) {
//            return true;
//        }

        return false;
    }

    private static double minZealots() {
        double fromLings = EnemyUnits.discovered().zerglings().count() * 0.29;

        if (A.hasGas(130)) fromLings = A.inRange(2, fromLings, 6);

        return A.inRange(4, fromLings, 9);
    }

    private static boolean transitionToDragoonsDueToMutas() {
        if (!Enemy.zerg()) return false;

        int mutas = EnemyUnits.count(AUnitType.Zerg_Mutalisk);

        if (mutas >= 2) {
            if (GamePhase.isEarlyGame()) return true;
            if (mutas >= 8) return true;
        }

        return false;
    }
}
