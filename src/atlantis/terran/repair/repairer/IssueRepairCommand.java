package atlantis.terran.repair.repairer;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class IssueRepairCommand extends Manager {
    private AUnit target;

    public IssueRepairCommand(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (target == null) return false;
        if (!A.hasMinerals(5)) return false;
        if (target.isAir() && target.distTo(unit) >= 2) return false;

        if (unit.isRepairing() && (target = unit.target()) != null && target.isAlive()) {
            return true;
        }

        target = RepairAssignments.unitToRepairForSCV(unit);

        if (target == null || !target.isAlive()) return false;

        return true;
    }

    @Override
    public Manager handle() {
        unit.repair(
            target,
            "Repair " + target.nameWithId() + "(" + unit.lastActionFramesAgo() + ")"
        );
        return usedManager(this);
    }
}
