package atlantis.terran.repair;

import atlantis.game.A;
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

        double maxRepairersAtOnce = Math.min(MAX_REPAIRERS_AT_ONCE, Count.workers() * 0.6);
        if (Count.workers() <= 10) maxRepairersAtOnce = Math.min(3, maxRepairersAtOnce);
        if (Count.workers() <= 10 && !A.hasMinerals(100)) maxRepairersAtOnce = Math.min(1, maxRepairersAtOnce);

        return repairers > maxRepairersAtOnce;
    }

}
