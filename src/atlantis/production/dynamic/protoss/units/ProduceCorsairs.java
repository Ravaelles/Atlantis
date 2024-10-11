package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceCorsairs {
    private static int produced = 0;

    public static boolean corsairs() {
        if (Have.notEvenPlanned(AUnitType.Protoss_Stargate)) {
            return false;
        }

        if (produced <= 1 && A.supplyUsed() >= 80 && Count.corsairs() == 0) {
            return buildToHave(AUnitType.Protoss_Corsair, 1) && increaseProduced();
        }

        int mutas = EnemyUnits.count(AUnitType.Zerg_Mutalisk);
        if (mutas >= 1) {
            return buildToHave(AUnitType.Protoss_Corsair, (int) (mutas / 2) + 1) && increaseProduced();
        }

        return false;
    }

    private static boolean increaseProduced() {
        produced++;
        return true;
    }
}
