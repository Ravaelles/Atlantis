package atlantis.information.decisions.protoss.dragoon;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class DragoonInsteadZealotVsZerg {
    public static boolean dragoonInsteadOfZealot_vZ() {
        if (!Enemy.zerg()) return false;

        if (transitionToDragoonsDueToMutas()) return true;
        if (notEnoughZealots()) return false;

//        if (A.minerals() >= 170 && Count.gateways() >= 3) return true;
//        if (A.hasGas(50) && !A.hasMinerals(225) && Count.dragoons() <= 2 && Count.zealots() >= 1) return true;

        return false;
    }

    private static boolean notEnoughZealots() {
        if (Count.zealots() <= minZealots()) return true;

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
        double fromLings = EnemyUnits.discovered().zerglings().count() * 0.24;

        if (A.hasGas(130)) fromLings = A.inRange(2, fromLings, 6);

        return Math.max(5, fromLings);
    }

    private static boolean transitionToDragoonsDueToMutas() {
        int mutas = EnemyUnits.count(AUnitType.Zerg_Mutalisk);

        if (mutas >= 3) {
            if (GamePhase.isEarlyGame()) return true;
            if (mutas >= 8) return true;
        }

        return false;
    }
}
