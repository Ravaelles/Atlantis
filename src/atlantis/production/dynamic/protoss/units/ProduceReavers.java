package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceReavers {
    public static boolean reavers() {
//        if (true) return false;

        if (A.supplyUsed() <= 70) return false;

        if (
            Have.no(AUnitType.Protoss_Robotics_Support_Bay)
                || Have.no(AUnitType.Protoss_Robotics_Facility)
        ) return false;

        int maxReavers = haveThisManyReavers();

        return buildToHave(AUnitType.Protoss_Reaver, maxReavers);
    }

    private static int haveThisManyReavers() {
        int reavers = Count.reavers();

        if (Count.ofType(AUnitType.Protoss_Shuttle) > 0 && reavers == 0) return 1;

        return
            A.inRange(
                0,
                (Decisions.isEnemyGoingAirAndWeAreNotPreparedEnough() ? 0 : (1 + A.supplyUsed() / 45)),
                3
            );
    }
}
