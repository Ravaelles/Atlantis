package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DontRunWhenBeingRepared extends Manager {
    public DontRunWhenBeingRepared(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        AUnit repairer = unit.repairer();
        return repairer != null && repairer.isRepairing() && repairer.distToLessThan(unit, 1);
    }

    @Override
    protected Manager handle() {
        if (unit.enemiesNear().melee().inRadius(1.9, unit).canAttack(unit, 5).notEmpty()) {
            return null;
        }

        if (
            !unit.woundPercentMin(50)
                && unit.enemiesNear().ranged().inRadius(7, unit).notEmpty()
        ) {
            return null;
        }

        AUnit repairer = unit.repairer();
        if (repairer != null && repairer.distToLessThan(unit, 1.1) && repairer.isRepairing()) {
            unit.move(repairer, Actions.MOVE_REPAIR, "BeFixed");
            return usedManager(this);
        }

        return null;
    }
}
