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

        AUnit repairer = unit.repairer();
        return repairer != null && repairer.isRepairing() && repairer.distToLessThan(unit, 2);
    }

    @Override
    protected Manager handle() {
//        if (unit.enemiesNear().melee().inRadius(1.9, unit).canAttack(unit, 5).notEmpty()) {
//            return null;
//        }
//
//        if (
//            !unit.woundPercentMin(50)
//                && unit.enemiesNear().ranged().inRadius(7, unit).notEmpty()
//        ) {
//            return null;
//        }

        AUnit repairer = unit.repairer();
        if (
            repairer != null
                && repairer.distToMoreThan(unit, 0.9)
        ) {
            unit.move(repairer, Actions.MOVE_REPAIR, "BeFixed");
            return usedManager(this);
        }

        return null;
    }
}
