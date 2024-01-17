package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

public class DragoonInsteadZealot {
    static boolean dragoonInsteadOfZealot() {
        if (!A.hasGas(50) || !Have.cyberneticsCore()) return false;
        if (notEnoughZealots()) return false;

        if (Enemy.zerg() && againstZerg()) return true;
        if (A.hasGas(50) && !A.hasMinerals(225) && Count.dragoons() <= 2 && Count.zealots() >= 1) return true;

        return false;
    }

    private static boolean notEnoughZealots() {
        if (
            Enemy.protoss()
                && OurArmyStrength.relative() <= 85
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
