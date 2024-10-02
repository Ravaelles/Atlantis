package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceReavers {
    public static boolean reavers() {
        if (true) return false;

        if (A.supplyUsed() <= 70) return false;

        if (
            Have.no(AUnitType.Protoss_Robotics_Facility)
                || Have.no(AUnitType.Protoss_Robotics_Support_Bay)
        ) return false;

        int maxReavers = haveThisManyReavers();

        return buildToHave(AUnitType.Protoss_Reaver, maxReavers);
    }

    private static int haveThisManyReavers() {
        return Decisions.isEnemyGoingAirAndWeAreNotPreparedEnough()
            ? 0
            : (1 + A.supplyUsed() / 45);
    }
}
