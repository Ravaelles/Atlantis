package atlantis.terran.repair.repairer;

import atlantis.architecture.Manager;
import atlantis.terran.repair.RepairAssignments;
import atlantis.terran.repair.ShouldNotRepairUnit;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class RemoveRepairer extends Manager {
    private AUnit target;

    public RemoveRepairer(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        target = RepairAssignments.unitToRepairForSCV(unit);

        if (target == null || !target.isAlive()) return true;
        if (unit.isProtector()) return false;

        if (unit.looksIdle() || (!unit.isRepairing() && !unit.isMoving())) return true;
        if (unit.lastActionMoreThanAgo(10, Actions.REPAIR)) return true;
        if (ShouldNotRepairUnit.shouldNotRepairUnit(unit, target)) return true;
        if (unit.lastActionMoreThanAgo(30 * 3, Actions.REPAIR)) return true;

        return false;
    }

    @Override
    public Manager handle() {
//        if (target.isHealthy() && !unit.isProtector()) {
//            unit.setTooltipTactical("Repaired!");
//            RepairAssignments.removeRepairer(unit);
//            return handleRepairCompletedTryFindingNewTarget();
//        }

        if (target == null || !target.isAlive() || target.isHealthy()) {
            RepairAssignments.removeRepairer(unit);
    
            return usedManager(this);
        }

        return null;
    }
}
