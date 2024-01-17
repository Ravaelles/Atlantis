package atlantis.production.dynamic.protoss.units;

import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceShuttles {
    public static void shuttles() {
        if (
            Have.notEvenPlanned(AUnitType.Protoss_Robotics_Facility)
                || Count.ofType(AUnitType.Protoss_Reaver) >= Count.ofType(AUnitType.Protoss_Shuttle)
        ) {
            return;
        }

        buildToHave(AUnitType.Protoss_Shuttle, 1);
    }
}
