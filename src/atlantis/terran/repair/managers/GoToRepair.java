package atlantis.terran.repair.managers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class GoToRepair extends Manager {
    public GoToRepair(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        AUnit repairer;

        return unit.canBeRepaired()
            && (unit.hp() <= 36 || ((repairer = unit.repairer()) != null && repairer.distTo(unit) <= 1))
            && !unit.isRunning();
    }

    public Manager handle() {
        if (unit.isAttacking()) return null;

        AUnit worker = defineClosestRepairer();

        if (worker != null) {
            if (moveToRepairer(worker)) return usedManager(this);
        }

        return null;
    }

    private boolean moveToRepairer(AUnit worker) {
        if (unit.distToMoreThan(worker, 0.2)) {
            unit.move(worker, Actions.MOVE_REPAIR, "Go2Repair");
        }
        else {
            unit.holdPosition("Go2Repair");
        }

        return true;
    }

    private AUnit defineClosestRepairer() {
        AUnit repairer = unit.repairer();

        if (repairer != null) return repairer;

        return Select.ourWorkersFreeToBuildOrRepair(true).nearestTo(unit);
    }
}
