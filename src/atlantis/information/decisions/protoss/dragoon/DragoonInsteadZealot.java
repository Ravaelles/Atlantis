package atlantis.information.decisions.protoss.dragoon;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class DragoonInsteadZealot {
    public static boolean dragoonInsteadOfZealot() {
        if (Enemy.zerg()) return DragoonInsteadZealotVsZerg.dragoonInsteadOfZealot_vZ();

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

        if (
            Enemy.zerg()
                && OurArmy.strength() <= 85
                && A.seconds() <= 400
//                && Count.zealots() <= Math.max(4, EnemyUnits.discovered().zealots().count() * 0.3)
//                && Count.zealots() <= Math.max(4, EnemyUnits.discovered().zealots().count() * 0.3)
                && Count.zealots() <= 3
                && EnemyUnits.discovered().zerglings().atLeast(9)
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
