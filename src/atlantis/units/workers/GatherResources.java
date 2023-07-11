package atlantis.units.workers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class GatherResources extends Manager {
    public GatherResources(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (handleGatherMineralsOrGas()) {
            return usedManager(this);
        }

        return null;
    }

    /**
     * Assigns given worker (which is idle by now at least doesn't have anything to do) to gather minerals.
     */
    private boolean handleGatherMineralsOrGas() {

        // Don't react if already gathering
        // @Check Surprisingly, isMiningOrExtractingGas is quite slow! looksIdle works faster
        if (!unit.looksIdle()) {
            unit.setTooltipTactical("Miner");
            return true;
        }

        if (unit.isRepairing()) {
            unit.setTooltipTactical("Repair");
            return true;
        }

        // If basically unit is not doing a shit, send it to gather resources (minerals or gas).
        // But check for multiple conditions (like if isn't constructing, repairing etc).
        if (unit.isIdle() || (!unit.isGatheringMinerals() && !unit.isGatheringGas() && !unit.isMoving()
            && !unit.isConstructing() && !unit.isAttackingOrMovingToAttack() && !unit.isRepairing())) {
            unit.setTooltipTactical("Move ass!");
            return AMineralGathering.gatherResources(unit);
        }

        return true;
    }
}
