package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

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
        if (unit.isRepairing()) {
            return true;
        }

        target = RepairAssignments.getUnitToProtectFor(unit);
//        System.out.println("protecting: " + target + "; "  + unit + " (" + unit.action() + ")");

        if (target == null || !target.isAlive()) {
            unit.setTooltipTactical("Null bunker");
            RepairAssignments.removeRepairer(unit);
            return true;
        }

        // WOUNDED
        if (target.isWounded() || target.enemiesNear().canAttack(target, 15).notEmpty()) {
            if (unit.isRepairing()) return true;

            return unit.repair(target, "Protect" + target.name(), true);
//                return unit.repair(Select.main(), "Protect" + target.name(), true);
//                return unit.doRightClickAndYesIKnowIShouldAvoidUsingIt(target);
        }

        // Bunker fully HEALTHY
        else {
            double distanceToUnit = target.distTo(unit);
            if (distanceToUnit > 0.7 && !unit.isMoving()) {
                return unit.move(
                    target.position(), Actions.MOVE_REPAIR, "ProtectNearer" + target.name(), true
                );
            }
            else {
                unit.setTooltipTactical("Protecting" + target.name());
            }
        }

        return false;
    }

    private Manager idleProtector() {
        if (target.isBunker()) return null;

        return (new IdleRepairer(unit)).handle();
    }
}
