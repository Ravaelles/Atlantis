package atlantis.workers;

import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisBuilderManager;
import atlantis.constructing.AtlantisConstructionManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.debug.AtlantisPainter;
import atlantis.units.AUnit;
import atlantis.units.Select;
import bwapi.Color;

public class AtlantisWorkerManager {

    /**
     * Executed for every worker unit.
     */
    public static void update(AUnit worker) {
        worker.removeTooltip();

        // =========================================================
        // Act as BUILDER
        if (AtlantisConstructionManager.isBuilder(worker)) {
            AtlantisBuilderManager.update(worker);
        } // ORDINARY WORKER
        else {
            sendToGatherMineralsOrGasIfNeeded(worker);
        }

        // =========================================================
        updateTooltip(worker);
    }

    // =========================================================
    /**
     * Assigns given worker unit (which is idle by now at least doesn't have anything to do) to gather
     * minerals.
     */
    private static void sendToGatherMineralsOrGasIfNeeded(AUnit worker) {

        // Don't react if already gathering
        if (worker.isGatheringGas() || worker.isGatheringMinerals()) {
            return;
        }

        worker.removeTooltip();

        // If is carrying minerals, return
        if (worker.isCarryingGas() || worker.isCarryingMinerals()) {
            worker.returnCargo();
            return;
        }

        // If basically unit is not doing a shit, send it to gather resources (minerals or gas).
        // But check for multiple conditions (like if isn't constructing, repairing etc).
        if (worker.isIdle()
                || (!worker.isGatheringMinerals() && !worker.isGatheringGas() && !worker.isMoving()
                && !worker.isConstructing() && !worker.isAttacking() && !worker.isRepairing())) {
            worker.setTooltip("Move ya ass!");
            AtlantisMineralGathering.gatherResources(worker);
        }
    }

    // =========================================================
    // Auxiliary
    /**
     * Returns total number of workers that are currently assigned to this building.
     */
    public static int getHowManyWorkersGatheringAt(AUnit target) {
        boolean isGasBuilding = target.getType().isGasBuilding();
        boolean isBase = target.isBase();
        int total = 0;

        for (AUnit worker : Select.ourWorkers().listUnits()) {
            if (isWorkerAssignedToBuilding(worker, target)) {
                total++;
            }
        }
        return total;
    }

    public static AUnit getRandomWorkerAssignedTo(AUnit target) {
        boolean isGasBuilding = target.getType().isGasBuilding();

        // Take those not carrying anything first
        for (AUnit worker : Select.ourWorkers().listUnits()) {
            if (!isWorkerAssignedToBuilding(worker, target)) {
                continue;
            }

            if (!worker.isCarryingGas() && !worker.isCarryingMinerals()) {
                return worker;
            }
        }

        // Meh, take those carrying as well
        for (AUnit worker : Select.ourWorkers().listUnits()) {
            if (isWorkerAssignedToBuilding(worker, target)) {
                return worker;
            }
        }

        return null;
    }
    
    public static boolean isWorkerAssignedToBuilding(AUnit worker, AUnit building) {
        if (building.equals(worker.getTarget()) || building.equals(worker.getOrderTarget())) {
            return true;
        } else if (building.equals(worker.getBuildUnit())) {
            return true;
        } 
        else if (building.getType().isGasBuilding()) {
            if (worker.isGatheringGas() && worker.distanceTo(building) <= 10) {
                return true;
            }
        }
        else if (building.isBase()) {
            if (worker.isGatheringMinerals() || worker.isCarryingMinerals()) {
                return true;
            } else if (worker.getTarget() != null && worker.getTarget().getType().isMineralField()) {
                return true;
            }
        }
        
        return false;
    }

    private static void updateTooltip(AUnit unit) {
        String tooltip = "";
        String newLine = "\r\n";
        //FIXME: this is making tooltip get the empty string
        ConstructionOrder buildingToBuild = AtlantisConstructionManager.getConstructionOrderFor(unit);
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
        unit.setTooltip(tooltip);
        //unit.setTooltip(tooltip);
    }

}
