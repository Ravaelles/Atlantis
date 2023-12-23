package atlantis.terran.repair.protect;

import atlantis.architecture.Manager;
import atlantis.terran.repair.IdleProtectorRepairs;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;

public class IdleProtector extends Manager {
    private AUnit target;

    public IdleProtector(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isProtector() && !unit.isRepairing() && unit.looksIdle();
    }

    @Override
    public Manager handle() {
        target = RepairAssignments.getUnitToProtectFor(unit);

        if (dontLeaveBunkerYouAreProtecting()) return null;

        return (new IdleProtectorRepairs(unit)).invoke(this);
    }

    private boolean dontLeaveBunkerYouAreProtecting() {
        return target.isBunker()
            && target.enemiesNear().havingWeapon().inRadius(6, target).atLeast(
            target.hp() <= 320 ? 1 : 3
        );
    }
}
