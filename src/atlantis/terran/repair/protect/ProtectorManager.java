package atlantis.terran.repair.protect;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.terran.repair.IdleProtectorRepairs;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.special.ManualUnitControlCommander;

/**
 * unit is a unit that is close to another unit (bunker or tank), ready to repair it,
 * even if it's not wounded (yet) or already repaired.
 */
public class ProtectorManager extends Manager {
    /**
     * Unit to protect.
     */
    private AUnit target;

    public ProtectorManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isProtector()) return false;

        target = RepairAssignments.getUnitToProtectFor(unit);

        if (target == null || !target.isAlive()) {
            unit.setTooltipTactical("Null bunker");
            RepairAssignments.removeRepairer(unit);
            return false;
        }

        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtectItsTarget.class,
            IdleProtector.class,
        };
    }
}
