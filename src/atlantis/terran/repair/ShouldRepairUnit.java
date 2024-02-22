package atlantis.terran.repair;

import atlantis.units.AUnit;

public class ShouldRepairUnit {
    public static boolean check(AUnit woundedUnit) {
        if (!woundedUnit.isMechanical()) return false;

        if (ShouldNotRepairUnit.shouldNotRepairUnit(null, woundedUnit)) {
            return false;
        }

        if (OptimalNumOfRepairers.hasUnitTooManyRepairers(woundedUnit)) {
            return false;
        }

        return woundedUnit.hp() <= 40;
    }
}
