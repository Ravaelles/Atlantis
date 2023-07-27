package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class IdleRepairer extends Manager {
    public IdleRepairer(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isScv() && unit.looksIdle();
    }

    @Override
    public Manager handle() {
        if (!unit.isUnitActionRepair() || !unit.isRepairing() || unit.isIdle()) {
            int maxAllowedDistToRoam = 13;

            // Try finding any repairable and wounded unit Near
            AUnit nearestWoundedUnit = Select.our().repairable(true).inRadius(maxAllowedDistToRoam, unit).nearestTo(unit);
            if (nearestWoundedUnit != null && A.hasMinerals(5)) {
                unit.repair(nearestWoundedUnit, "HelpNear" + nearestWoundedUnit.name(), true);
                if (nearestWoundedUnit.distTo(unit) > 0.8) {
                    nearestWoundedUnit.move(unit, Actions.MOVE_REPAIR, "BeHelped");
                }
                return usedManager(this);
            }
        }

        return null;
    }
}
