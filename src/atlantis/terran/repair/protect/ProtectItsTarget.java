package atlantis.terran.repair.protect;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;

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

        if (target.isWounded() || (unit.isBunker() && A.everyNthGameFrame(7))) {
            if (unit.repair(target, "Protect" + target.name())) return usedManager(this);
        }

        return null;
    }
}
