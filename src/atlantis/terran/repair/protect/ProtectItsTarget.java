package atlantis.terran.repair.protect;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.workers.GatherResources;

public class ProtectItsTarget extends Manager {
    private AUnit target;

    public ProtectItsTarget(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isProtector();
    }

    @Override
    public Manager handle() {
        target = RepairAssignments.getUnitToProtectFor(unit);

        if (target != null && target.isAlive()) {
//            System.err.println("unit.distTo(target) = " + unit.distToDigit(target));

            if (
                target.isWounded()
                    || target.isBunker()
                    || A.everyNthGameFrame(11)
            ) {
                if (A.hasMinerals(1)) {
                    unit.repair(target, "Protect" + target.name());
                    return usedManager(this);
                }
                else {
                    if (unit.distTo(target) > 0.1) {
                        if (unit.move(target, Actions.MOVE_PROTECT)) {
                            return usedManager(this, "MoveProtect" + target.name());
                        }
                    }

                    if (unit.id() % 4 != 0) removeRepairer();
                }
            }
        }
        else {
            removeRepairer();
        }

        return null;
    }

    private void removeRepairer() {
        RepairAssignments.removeProtector(unit);

        (new GatherResources(unit)).forceHandle();
    }
}
