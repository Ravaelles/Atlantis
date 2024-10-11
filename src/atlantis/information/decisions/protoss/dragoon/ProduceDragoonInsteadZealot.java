package atlantis.information.decisions.protoss.dragoon;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

public class ProduceDragoonInsteadZealot {
    public static boolean dragoonInsteadOfZealot() {
        if (ZealotFocus.check()) return false;

//        if (Count.dragoons() == 0 && A.gas(50) && Have.cyberneticsCore() && )
        if (Count.dragoons() <= 10 && A.hasGas(50) && Have.cyberneticsCore()) return true;
        if (Enemy.zerg()) return DragoonInsteadZealotVsZerg.dragoonInsteadOfZealot_vZ();

        if (preferZealotWhenNotEnoughGasAndManyMinerals()) return false;
        if (manyZealotsAndEnemyTooStrong()) return true;

//        if (Have.cyberneticsCore()) return false;
//        if (!A.hasGas(50) || !Have.cyberneticsCore()) return false;
        if (notEnoughZealots()) return false;

        if (Enemy.protoss() && Count.ourCombatUnits() >= 9 && OurArmy.strength() >= 70) return true;
        if (A.minerals() >= 170 && Count.gateways() >= 3) return true;
        if (Enemy.zerg() && dragoonAgainstZerg()) return true;
        if (A.hasGas(50) && !A.hasMinerals(225) && Count.dragoons() <= 2 && Count.zealots() >= 1) return true;

        return false;
    }

    private static boolean preferZealotWhenNotEnoughGasAndManyMinerals() {
        int minerals = A.minerals();
        int gas = A.gas();

        if (minerals >= 700 && gas <= 180 && Count.zealots() <= 2 && Count.freeGateways() >= 2) return true;

        return gas < 125 && minerals >= 650 && Count.ourCombatUnits() >= 10;
    }

    private static boolean manyZealotsAndEnemyTooStrong() {
        return Enemy.protoss()
            && OurArmy.strength() <= 120
            && Count.zealots() >= 9;
    }

    private static boolean notEnoughZealots() {
        if (
            Enemy.protoss()
                && OurArmy.strength() <= 85
                && A.seconds() <= 400
//                && Count.zealots() <= Math.max(4, EnemyUnits.discovered().zealots().count() * 0.3)
//                && Count.zealots() <= Math.max(4, EnemyUnits.discovered().zealots().count() * 0.3)
                && (!A.hasGas(100) && Count.zealots() <= 2)
                && (Count.freeGateways() <= 1 || EnemyUnits.discovered().dragoons().atMost(1))
        ) {
            return true;
        }

        return false;
    }

    private static boolean dragoonAgainstZerg() {
        int mutas = EnemyUnits.count(AUnitType.Zerg_Mutalisk);
        if (mutas >= 3) {
            if (GamePhase.isEarlyGame()) return true;
            if (mutas >= 8) return true;
        }

        return false;
    }
}
