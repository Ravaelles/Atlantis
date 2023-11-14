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

        if (unit.idIsOdd()) return usedManager(this); // Only half of the protectors can do dynamic repairs if idle

        if (
            target.isBunker() && target.enemiesNear().havingWeapon().inRadius(9, target).atMost(1)
        ) return null;

        return (new IdleProtectorRepairs(unit)).invoke();
    }
}
