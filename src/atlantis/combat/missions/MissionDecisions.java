package atlantis.combat.missions;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class MissionDecisions {
    public static boolean baseUnderSeriousAttack() {
        AUnit main = Select.main();
        if (main != null && Select.enemyCombatUnits().inRadius(20, main).atLeast(minEnemiesToDefend())) {
            return true;
        }

        return false;
    }

    private static int minEnemiesToDefend() {
        if (A.supplyUsed() < 30) {
            return 1;
        }
        else if (A.supplyUsed() < 50) {
            return 2;
        }
        else if (A.supplyUsed() < 80) {
            return 3;
        }
        else {
            return 6;
        }
    }
}
