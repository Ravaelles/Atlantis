package atlantis.production.dynamic.protoss.units;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceCorsairs {
    public static void corsairs() {
        if (Have.notEvenPlanned(AUnitType.Protoss_Stargate)) {
            return;
        }

        int mutas = EnemyUnits.count(AUnitType.Zerg_Mutalisk);
        if (mutas >= 1) {
            buildToHave(AUnitType.Protoss_Corsair, (int) (mutas / 2) + 1);
        }
    }
}
