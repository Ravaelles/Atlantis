package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DontMoveWhenBeingRepared extends Manager {
    public DontMoveWhenBeingRepared(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isHealthy()) return false;
        if (unit.meleeEnemiesNearCount(2.5) > 0) return false;

        AUnit repairer = unit.repairer();
        return repairer != null && repairer.isRepairing() && repairer.distToLessThan(unit, 2);
    }

    @Override
    protected Manager handle() {

        AUnit repairer = unit.repairer();
        if (
            repairer != null && repairer.distToMoreThan(unit, 0.4)
        ) {
            unit.move(repairer, Actions.MOVE_REPAIR, "BeFixed");
            return usedManager(this);
        }

        return null;
    }
}
