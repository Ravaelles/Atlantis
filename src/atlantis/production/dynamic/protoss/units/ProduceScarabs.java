package atlantis.production.dynamic.protoss.units;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.List;

public class ProduceScarabs {
    public static void scarabs() {
        List<AUnit> reavers = Select.ourOfType(AUnitType.Protoss_Reaver).list();
        for (AUnit reaver : reavers) {
            if (reaver.scarabCount() <= 3 && !reaver.isTrainingAnyUnit()) {
                reaver.trainForced(AUnitType.Protoss_Scarab);
            }
        }
    }
}
