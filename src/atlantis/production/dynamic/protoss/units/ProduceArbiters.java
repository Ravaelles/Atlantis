package atlantis.production.dynamic.protoss.units;

import atlantis.units.AUnitType;
import atlantis.units.select.Count;

import static atlantis.production.AbstractDynamicUnits.trainNowIfHaveWhatsRequired;

public class ProduceArbiters {
    public static void arbiters() {
        if (Count.ofType(AUnitType.Protoss_Arbiter_Tribunal) == 0) {
            return;
        }

        trainNowIfHaveWhatsRequired(AUnitType.Protoss_Arbiter, true);
    }
}
