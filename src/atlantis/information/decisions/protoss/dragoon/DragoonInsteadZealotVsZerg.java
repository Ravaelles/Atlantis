package atlantis.information.decisions.protoss.dragoon;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class DragoonInsteadZealotVsZerg {
    public static boolean dragoonInsteadOfZealot() {
        if (!Enemy.zerg()) return false;

        if (transitionToDragoonsDueToMutas()) return true;
        if (notEnoughZealots()) return false;

//        if (A.minerals() >= 170 && Count.gateways() >= 3) return true;
//        if (A.hasGas(50) && !A.hasMinerals(225) && Count.dragoons() <= 2 && Count.zealots() >= 1) return true;

        return false;
    }

    private static boolean notEnoughZealots() {
        if (
            OurArmy.strength() <= 85
                && A.seconds() <= 400
                && Count.zealots() <= Math.max(4, EnemyUnits.discovered().zerglings().count() * 0.3)
                && EnemyUnits.discovered().dragoons().atMost(1)
        ) {
            return true;
        }

        return false;
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
