package atlantis.workers;

import java.util.Collection;

import atlantis.constructing.AtlantisBuilderManager;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.util.NameUtil;
import atlantis.util.UnitUtil;
import atlantis.wrappers.Select;
import bwapi.Unit;

public class AtlantisWorkerManager {

    /**
     * Executed for every worker unit.
     */
    public static void update(Unit unit) {
        TooltipManager.removeTooltip(unit);
        //unit.removeTooltip();

        // Act as BUILDER
        if (AtlantisConstructingManager.isBuilder(unit)) {
            AtlantisBuilderManager.update(unit);
        } // ORDINARY WORKER
        else {
            sendToGatherMineralsOrGasIfNeeded(unit);
        }

        updateTooltip(unit);
    }

    // =========================================================
    /**
     * Assigns given worker unit (which is idle by now at least doesn't have anything to do) to gather
     * minerals.
     */
    private static void sendToGatherMineralsOrGasIfNeeded(Unit worker) {

        // If basically unit is not doing a shit, send it to gather resources (minerals or gas).
        // But check for multiple conditions (like if isn't constructing, repairing etc).
        if (worker.isIdle()
                || (!worker.isGatheringMinerals() && !worker.isGatheringGas() && !worker.isMoving()
                && !worker.isConstructing() && !worker.isAttacking() && !worker.isRepairing())) {
            TooltipManager.setTooltip(worker, "Move ya ass!");
            AtlantisMineralGathering.gatherResources(worker);
        }
    }

    // =========================================================
    // Auxiliary
    public static int getHowManyWorkersAt(Unit target) {
        boolean isGasBuilding = UnitUtil.isGasBuilding(target.getType());
        int total = 0;
        Collection<Unit> ourWorkersInRange = (Collection<Unit>) Select.ourWorkers().inRadius(15, target.getPosition()).listUnits();
        for (Unit worker : ourWorkersInRange) {
            if (target.equals(worker.getTarget())) {
                total++;
            } else if (target.equals(worker.getOrderTarget())) {
                total++;
            } else if (target.equals(worker.getBuildUnit())) {
                total++;
            } else if (isGasBuilding) {
                if (worker.isCarryingGas() || worker.isGatheringGas()) {
                    total++;
                }
            }
        }
        return total;
    }

    public static Unit getRandomWorkerAssignedTo(Unit target) {
        for (Unit worker : Select.ourWorkers().listUnits()) {
            if (target.equals(worker.getTarget()) || target.equals(worker.getOrderTarget())
                    || target.equals(worker.getBuildUnit())) {
                return worker;
            }
        }

        return null;
    }

    private static void updateTooltip(Unit unit) {
        String tooltip = "";
        String newLine = "\r\n";
        //FIXME: this is making tooltip get the empty string
        ConstructionOrder buildingToBuild = AtlantisConstructingManager.getConstructionOrderFor(unit);
        if (buildingToBuild != null) {
            tooltip += "Build: " + buildingToBuild.getBuildingType().getShortName() + newLine;
        }
//		if (unit.getTarget() != null) {
//			tooltip += "Target: " + unit.getTarget().getShortName() + newLine;
//		}
//		if (unit.getOrderTarget() != null) {
//			tooltip += "OrderTarget: " + unit.getOrderTarget().getShortName() + newLine;
//		}
        // if (unit.isGatheringMinerals()) {
        // tooltip += "Minerals" + newLine;
        // }
        // if (unit.isGatheringGas()) {
        // tooltip += "Gas" + newLine;
        // }
        // if (unit.isConstructing()) {
        // tooltip += "Constructing" + newLine;
        // }
        // if (unit.isRepairing()) {
        // tooltip += "Repairing" + newLine;
        // }
        // if (unit.isMoving()) {
        // tooltip += "Moving" + newLine;
        // }
        // if (unit.isAttacking()) {
        // tooltip += "Attacking" + newLine;
        // }
        // if (unit.isStartingAttack()) {
        // tooltip += "StartingAttack" + newLine;
        // }
        // if (unit.isIdle()) {
        // tooltip += "Idle" + newLine;
        // }
        TooltipManager.setTooltip(unit, tooltip);
        //unit.setTooltip(tooltip);
    }

}
