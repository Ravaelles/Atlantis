package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

public class DragoonInsteadZealot {
    public static boolean dragoonInsteadOfZealot() {
        if (manyZealotsAndEnemyTooStrong()) return true;

        if (!A.hasGas(50) || !Have.cyberneticsCore()) return false;
        if (notEnoughZealots()) return false;

        if (A.minerals() >= 150 && Count.freeGateways() >= 2 && Count.basesWithUnfinished() >= 2) return false;
        if (Enemy.zerg() && againstZerg()) return true;
        if (A.hasGas(50) && !A.hasMinerals(225) && Count.dragoons() <= 2 && Count.zealots() >= 1) return true;

        return false;
    }

    private static boolean manyZealotsAndEnemyTooStrong() {
        return Enemy.protoss()
            && OurArmy.strength() <= 120
            && Count.zealots() >= 7;
    }

    private static boolean notEnoughZealots() {
        if (
            Enemy.protoss()
                && OurArmy.strength() <= 85
                && A.seconds() <= 400
                && Count.zealots() <= 1
                && EnemyUnits.discovered().dragoons().atMost(1)
        ) {
            return true;
        }

        return false;
    }

    private static boolean againstZerg() {
        int mutas = EnemyUnits.count(AUnitType.Zerg_Mutalisk);
        if (mutas >= 3) {
            if (GamePhase.isEarlyGame()) return true;
            if (mutas >= 8) return true;
        }

        return false;
    }
}
