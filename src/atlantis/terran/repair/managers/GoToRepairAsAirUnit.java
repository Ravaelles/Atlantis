package atlantis.terran.repair.managers;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;

public class GoToRepairAsAirUnit extends Manager {
    private AUnit repairer;

    public GoToRepairAsAirUnit(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir()
            && A.hasMinerals(1)
            && unit.isMechanical()
            && unit.isWounded()
            && (
            unit.hp() <= minHealth()
                || ((repairer = unit.repairer()) != null && repairer.distTo(unit) <= 1)
                || RepairAssignments.countRepairersForUnit(unit) >= 2
        );
    }

    public Manager handle() {
        if (repairer != null) {
            if (moveToRepairer(repairer)) return usedManager(this);
        }

        AUnit worker = defineClosestRepairer();
        if (worker != null) {
            if (moveToRepairer(worker)) return usedManager(this);
        }

        return null;
    }

    private int minHealth() {
        return unit.maxHp() >= 150 ? 60 : 36;
    }

    private boolean moveToRepairer(AUnit worker) {
        if (unit.distToMoreThan(worker, 0.2)) {
            unit.move(worker, Actions.MOVE_REPAIR, "Go2Repair");
        }
        else {
            unit.holdPosition(Actions.HOLD_POSITION, "Go2Repair");
        }

        return true;
    }

    private AUnit defineClosestRepairer() {
        AUnit repairer = unit.repairer();

        if (repairer != null) return repairer;

        AUnit protector = Select.ourWorkers().protectors().notRepairing().healthy().nearestTo(unit);
        if (protector != null) return protector;

        return FreeWorkers.get().nearestTo(unit);
    }
}
