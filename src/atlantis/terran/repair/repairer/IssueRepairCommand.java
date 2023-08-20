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
        if (unit.isRepairing() || !A.hasMinerals(3)) return false;

        target = RepairAssignments.unitToRepairForSCV(unit);

        return target.isAlive();
    }

    @Override
    public Manager handle() {
//        if (unit.lastActionMoreThanAgo(30 * 3, Actions.REPAIR) || unit.looksIdle()) {
//            RepairAssignments.removeRepairer(unit);
//            unit.setTooltipTactical("IdleGTFO");
//            unit.gatherBestResources();
//            return usedManager(this);
//        }

        unit.repair(
            target,
            "Repair " + target.nameWithId() + "(" + unit.lastActionFramesAgo() + ")"
        );
        return usedManager(this);
    }
}
