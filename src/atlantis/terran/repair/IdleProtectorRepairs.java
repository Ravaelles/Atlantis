package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class IdleProtectorRepairs extends Manager {
    public IdleProtectorRepairs(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isScv() && unit.looksIdle();
    }

    @Override
    protected Manager handle() {
        if (!unit.isUnitActionRepair() || !unit.isRepairing() || unit.isIdle()) {
            int maxAllowedDistToRoam = 13;

            // Try finding repairable tanks nearby
            if (repairTanks(maxAllowedDistToRoam)) return usedManager(this);

            // Try finding any repairable nearby
            if (repairAny(maxAllowedDistToRoam)) return usedManager(this);
        }

        return null;
    }

    private boolean repairTanks(int maxAllowedDistToRoam) {
        AUnit nearestWoundedUnit = RepairableUnits.get().tanks().inRadius(maxAllowedDistToRoam, unit).nearestTo(unit);
        if (nearestWoundedUnit != null && A.hasMinerals(2)) {
            unit.repair(nearestWoundedUnit, "HelpNear" + nearestWoundedUnit.name());
            return true;
        }
        return false;
    }

    private boolean repairAny(int maxAllowedDistToRoam) {
        AUnit nearestWoundedUnit = RepairableUnits.get().inRadius(maxAllowedDistToRoam, unit).nearestTo(unit);
        if (nearestWoundedUnit != null && A.hasMinerals(5)) {
            unit.repair(nearestWoundedUnit, "HelpNear" + nearestWoundedUnit.name());
            return true;
        }
        return false;
    }
}
