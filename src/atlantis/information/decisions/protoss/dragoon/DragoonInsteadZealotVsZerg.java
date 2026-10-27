package atlantis.information.decisions.protoss.dragoon;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class DragoonInsteadZealotVsZerg {
    public static boolean dragoonInsteadOfZealot_vZ() {
        if (!Enemy.zerg()) return false;

        if (A.gas() >= 50) return true;
        if (transitionToDragoonsDueToMutas()) return true;

        return A.hasGas(30) && Count.cannonsWithUnfinished() >= 3;

//        if (A.minerals() >= 170 && Count.gateways() >= 3) return true;
//        if (A.hasGas(50) && !A.hasMinerals(225) && Count.dragoons() <= 2 && Count.zealots() >= 1) return true;
    }

    private static boolean transitionToDragoonsDueToMutas() {
        if (!Enemy.zerg()) return false;

        int mutas = EnemyUnits.count(AUnitType.Zerg_Mutalisk);

        if (mutas >= 2) {
            if (GamePhase.isEarlyGame()) return true;

            return mutas >= 8;
        }

        return false;
    }
}
