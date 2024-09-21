package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceReavers {
    public static void reavers() {
        if (A.supplyUsed() <= 70) return;

        if (
            Have.no(AUnitType.Protoss_Robotics_Facility)
                || Have.no(AUnitType.Protoss_Robotics_Support_Bay)
        ) return;

        int maxReavers = haveThisManyReavers();

        buildToHave(AUnitType.Protoss_Reaver, maxReavers);
    }

    private static int haveThisManyReavers() {
        return Decisions.isEnemyGoingAirAndWeAreNotPreparedEnough()
            ? 0
            : (1 + A.supplyUsed() / 45);
    }
}
