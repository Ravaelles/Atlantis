package atlantis.information.decisions.protoss.dragoon;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.GamePhase;
import atlantis.production.dynamic.protoss.units.ProduceZealot;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

public class DragoonInsteadZealotVsZerg {
    public static boolean dragoonInsteadOfZealot_vZ() {
        if (!Enemy.zerg()) return false;

        if (A.gas() >= 50) return true;
        if (transitionToDragoonsDueToMutas()) return true;

        if (A.hasGas(30) && Count.cannonsWithUnfinished() >= 3) return true;

        if (ProduceZealot.notEnoughZealots()) return false;
        if (ProduceZealot.enoughZealots()) return false;

//        if (A.minerals() >= 170 && Count.gateways() >= 3) return true;
//        if (A.hasGas(50) && !A.hasMinerals(225) && Count.dragoons() <= 2 && Count.zealots() >= 1) return true;

        return false;
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
