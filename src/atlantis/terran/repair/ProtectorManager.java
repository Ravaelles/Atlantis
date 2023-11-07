package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

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
        return unit.isProtector();
    }

    protected Manager handle() {
        if (act()) return usedManager(this);

        return idleProtector();
    }

    private boolean act() {
        if (unit.isRepairing()) return true;

        target = RepairAssignments.getUnitToProtectFor(unit);

        if (target == null || !target.isAlive()) {
            unit.setTooltipTactical("Null bunker");
            RepairAssignments.removeRepairer(unit);
            return true;
        }

        // WOUNDED
        if (target.isWounded() || (unit.isBunker() && A.everyNthGameFrame(7))) {
            return unit.repair(target, "Protect" + target.name());
//                return unit.repair(Select.main(), "Protect" + target.name(), true);
//                return unit.doRightClickAndYesIKnowIShouldAvoidUsingIt(target);
        }

        // Bunker fully HEALTHY
//        double distanceToUnit = target.distTo(unit);
//        if (distanceToUnit > 0.7 && !unit.isMoving()) {
//            return unit.move(
//                target.position(), Actions.MOVE_REPAIR, "ProtectNearer" + target.name(), true
//            );
//        }
//        else {
//            unit.setTooltipTactical("Protecting" + target.name());
//        }

        return false;
    }

    private Manager idleProtector() {
        if (unit.idIsOdd()) return usedManager(this); // Only half of the protectors can do dynamic repairs if idle

        if (
            target.isBunker() && target.enemiesNear().havingWeapon().inRadius(9, target).atMost(1)
        ) return null;

        return (new IdleProtectorRepairs(unit)).invoke();
    }
}
