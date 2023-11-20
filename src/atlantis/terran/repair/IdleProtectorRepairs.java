package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class IdleProtectorRepairs extends Manager {

    private Selection repairable;

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

            // Try finding repairable tanks nearby
            if (repairTanks(13)) return usedManager(this);

            // Try finding any repairable nearby
            if (repairAnyGround(7)) return usedManager(this);

            // Try finding any repairable nearby
            if (repairAir(4)) return usedManager(this);
        }

        return null;
    }

    private boolean repairTanks(int maxAllowedDistToRoam) {
        repairable = RepairableUnits.get();
        AUnit nearestWoundedUnit = repairable.tanks().inRadius(maxAllowedDistToRoam, unit).nearestTo(unit);

        if (nearestWoundedUnit != null && A.hasMinerals(2)) {
            unit.repair(nearestWoundedUnit, "HelpTank:" + nearestWoundedUnit.name());
            return true;
        }
        return false;
    }

    private boolean repairAnyGround(int maxAllowedDistToRoam) {
        AUnit nearestWoundedUnit = RepairableUnits.get()
            .groundUnits()
            .inRadius(maxAllowedDistToRoam, unit)
            .nearestTo(unit);

        if (nearestWoundedUnit != null && A.hasMinerals(5)) {
            unit.repair(nearestWoundedUnit, "HelpGround:" + nearestWoundedUnit.name());
            return true;
        }
        return false;
    }

    private boolean repairAir(int maxAllowedDistToRoam) {
        AUnit nearestWoundedUnit = RepairableUnits.get()
            .inRadius(maxAllowedDistToRoam, unit)
            .nearestTo(unit);

        if (nearestWoundedUnit != null && A.hasMinerals(5)) {
            unit.repair(nearestWoundedUnit, "HelpAir:" + nearestWoundedUnit.name());
            return true;
        }
        return false;
    }
}
