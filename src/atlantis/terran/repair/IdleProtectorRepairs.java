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

            // Try finding any repairable and wounded unit Near
            AUnit nearestWoundedUnit = RepairableUnits.get().inRadius(maxAllowedDistToRoam, unit).nearestTo(unit);
            if (nearestWoundedUnit != null && A.hasMinerals(5)) {
                unit.repair(nearestWoundedUnit, "HelpNear" + nearestWoundedUnit.name());
//                if (nearestWoundedUnit.distTo(unit) > 0.8) {
//                    nearestWoundedUnit.move(unit, Actions.MOVE_REPAIR, "BeHelped");
//                }
                return usedManager(this);
            }
        }

        return null;
    }
}
