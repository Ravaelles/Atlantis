package atlantis.production.dynamic.protoss.units;

import atlantis.information.decisions.Decisions;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceReavers {
    public static void reavers() {
        if (true) return;

        if (
            Have.notEvenPlanned(AUnitType.Protoss_Robotics_Facility)
                || Have.notEvenPlanned(AUnitType.Protoss_Robotics_Support_Bay)
        ) return;

        int maxReavers = Decisions.isEnemyGoingAirAndWeAreNotPreparedEnough() ? 0 : 5;

        buildToHave(AUnitType.Protoss_Reaver, maxReavers);
    }
}
