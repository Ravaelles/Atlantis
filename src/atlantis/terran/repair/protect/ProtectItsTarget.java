package atlantis.terran.repair.protect;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
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
            if (target.isWounded() || target.isBunker() || A.everyNthGameFrame(11)) {
                if (A.hasMinerals(1)) {
                    unit.repair(target, "Protect" + target.name());
                    return usedManager(this);
                }
            }

            removeRepairer();
        }

        return null;
    }

    private void removeRepairer() {
        RepairAssignments.removeRepairer(unit);
        (new GatherResources(unit)).forceHandle();
    }
}
