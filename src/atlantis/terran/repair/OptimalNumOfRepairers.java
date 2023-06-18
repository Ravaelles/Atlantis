package atlantis.terran.repair;

import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class OptimalNumOfRepairers {

    public static final int MAX_REPAIRERS_AT_ONCE = 9;

    // =========================================================

    public static boolean hasUnitTooManyRepairers(AUnit unit) {
        return ARepairAssignments.countRepairersForUnit(unit) >= RepairerAssigner.optimalRepairersFor(unit);
    }

    public static boolean weHaveTooManyRepairersOverall() {
        return ARepairAssignments.countTotalRepairers() > Math.min(MAX_REPAIRERS_AT_ONCE, Count.workers() * 2 / 3);
    }

}
