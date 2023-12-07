package atlantis.terran.repair.repairer;

import atlantis.architecture.Manager;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class SeparateFromRunningTanks extends Manager {
    private AUnit target;

    public SeparateFromRunningTanks(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isMiningOrExtractingGas() || !unit.isScv()) return false;

        target = RepairAssignments.unitToRepairForSCV(unit);

        if (target == null) return false;

        return target.isTankUnsieged()
            && target.isRunning()
            && unit.distTo(target) <= 1.4
            && target.enemiesNear().groundUnits().canAttack(target, 1.4).notEmpty();
    }

    @Override
    public Manager handle() {
        unit.runningManager().runFrom(target, 0.5, Actions.MOVE_SPACE, false);
        return usedManager(this);
    }
}
