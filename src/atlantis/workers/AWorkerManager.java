package atlantis.workers;

import atlantis.combat.micro.AAvoidMeleeUnitsManager;
import atlantis.constructing.ABuilderManager;
import atlantis.constructing.AConstructionManager;
import atlantis.repair.ARepairManager;
import atlantis.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.Select;

public class AWorkerManager {

    /**
     * Executed for every worker unit.
     */
    public static boolean update(AUnit worker) {
        worker.removeTooltip();
        if (AScoutManager.isScout(worker)) {
            return false;
        }
        if (ARepairManager.isRepairerOfAnyKind(worker)) {
            return false;
        }
        
        // =========================================================
        // === Worker micro ========================================
        
        if (AAvoidMeleeUnitsManager.avoidCloseMeleeUnits(worker)) {
            return true;
        }
        
        // === END OF Worker micro =================================
        // =========================================================

        // =========================================================
        // Act as BUILDER if needed
        if (AConstructionManager.isBuilder(worker)) {
            ABuilderManager.update(worker);
            if (worker.getTooltip() == null) {
                worker.setTooltip("Builder");
            }
            return true;
        } 

        // ORDINARY WORKER
        else {
            sendToGatherMineralsOrGasIfNeeded(worker);
            worker.setTooltip("Gather");
            return true;
        }
    }

    // =========================================================
    /**
     * Assigns given worker unit (which is idle by now at least doesn't have anything to do) to gather
     * minerals.
     */
    private static void sendToGatherMineralsOrGasIfNeeded(AUnit worker) {
        worker.removeTooltip();

        // Don't react if already gathering
        if (worker.isGatheringGas() || worker.isGatheringMinerals()) {
            return;
        }

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
            AMineralGathering.gatherResources(worker);
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

}
