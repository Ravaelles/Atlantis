package atlantis.terran.repair;

import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class OptimalNumOfRepairers {

    public static final int MAX_REPAIRERS_AT_ONCE = 9;

    // =========================================================

    public static boolean hasUnitTooManyRepairers(AUnit unit) {
        return RepairAssignments.countRepairersForUnit(unit) >= NewRepairsCommander.optimalNumOfRepairersFor(unit);
    }

    public static boolean weHaveTooManyRepairersOverall() {
        int repairers = RepairAssignments.countTotalRepairers() + RepairAssignments.countTotalProtectors();
        
        return repairers > Math.min(MAX_REPAIRERS_AT_ONCE, Count.workers() * 0.6);
    }

}
