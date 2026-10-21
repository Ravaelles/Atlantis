package atlantis.units.workers.gather;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class GatherResources extends Manager {
    public GatherResources(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit == null || unit.isDead()) return false;
        if (!unit.isWorker()) return false;

        // A unit the engine still reports as not completed (a Probe still training
        // in the Nexus) cannot take ANY order: JBWAPI canCommand is false until
        // completion, so every gather/rightClick is refused and lastCommand stays
        // NO_COMMAND forever (measured on OpenBW: one such worker stood from frame
        // 30 to the end, NEXT #63). Issuing anyway only poisons our own
        // GATHER_MINERALS action flag, which then locks the re-try path that
        // checks !isGatheringResources. Leave it alone until it is born.
        if (!unit.isCompleted()) return false;

        if (!unit.isStopped() && unit.isGatheringResources()) {
            if (unit.lastPositionChangedLessThanAgo(10)) return false;
        }

        if (unit.isAttacking() && unit.hasValidTarget()) return false;

        return !unit.isGatheringGas()
            && !unit.isGatheringMinerals()
            && !unit.isBuilder()
            && !unit.isRunning()
            && !unit.isConstructing()
            && !unit.isRepairing()
            && !unit.isProtector()
            && !unit.isScout()
            && unit.lastActionMoreThanAgo(20, Actions.REPAIR)
            && unit.lastActionMoreThanAgo(50, Actions.SPECIAL)
            && unit.enemiesNear().inRadius(2 + unit.woundPercent() / 40.0, unit).combatUnits().empty();
    }

    protected Manager handle() {
//        if (itIsNotSafeToGather()) return null;

        if (handleGatherMineralsOrGas()) return usedManager(this);

        if ((new GatherFallback(unit)).forceHandle() != null) return usedManager(this);

        return null;
    }

//    private boolean itIsNotSafeToGather() {
//        return unit.enemiesNear().combatUnits().inRadius(2 + unit.woundPercent() / 30.0, unit).notEmpty();
//    }

    /**
     * Assigns given worker (which is idle by now at least doesn't have anything to do) to gather minerals.
     */
    private boolean handleGatherMineralsOrGas() {
        unit.setTooltipTactical("Move ass!");
        return AMineralGathering.gatherResources(unit);

        // If basically unit is not doing a shit, send it to gather resources (minerals or gas).
        // But check for multiple conditions (like if isn't constructing, repairing etc).
//        if (unit.isIdle() || (!unit.isGatheringMinerals() && !unit.isGatheringGas() && !unit.isMoving()
//            && !unit.isConstructing() && !unit.isAttackingOrMovingToAttack() && !unit.isRepairing())) {
//        }
//
//        return true;
    }
}
