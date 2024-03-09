package atlantis.units.workers;

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

        return !unit.isGatheringGas()
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
        if (itIsNotSafeToGather()) return null;

        if (handleGatherMineralsOrGas()) return usedManager(this);

        return null;
    }

    private boolean itIsNotSafeToGather() {
        return unit.lastStartedRunningLessThanAgo(100)
            || unit.enemiesNear().combatUnits().inRadius(2 + unit.woundPercent() / 30.0, unit).notEmpty();
    }

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
