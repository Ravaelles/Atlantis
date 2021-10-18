package atlantis.workers;

import atlantis.combat.micro.avoid.AAvoidEnemyMeleeUnitsManager;
import atlantis.combat.micro.avoid.AAvoidInvisibleEnemyUnits;
import atlantis.constructing.ABuilderManager;
import atlantis.constructing.AConstructionManager;
import atlantis.repair.ARepairAssignments;
import atlantis.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.Select;

public class AWorkerManager {

    /**
     * Executed for every worker unit.
     */
    public static boolean update(AUnit worker) {
        worker.removeTooltip();

        if (AAvoidInvisibleEnemyUnits.avoid(worker)) {
            return true;
        }

        if (AAvoidEnemyMeleeUnitsManager.shouldRunFromAnyEnemyMeleeUnit(worker)) {
            return true;
        }

        if (workerManagerForbiddenFor(worker)) {
            return false;
        }

        // Act as BUILDER
        if (AConstructionManager.isBuilder(worker)) {
            ABuilderManager.update(worker);
            if (worker.getTooltip() == null) {
                worker.setTooltip("Builder");
            }
            return true;
        } 

        // Ordinary WORKER
        else {
            handleGatherMineralsOrGas(worker);
            worker.setTooltip("Gather");
            return true;
        }
    }

    // =========================================================

    private static boolean workerManagerForbiddenFor(AUnit worker) {
        if (AScoutManager.isScout(worker)) {
            return true;
        }

        if (ARepairAssignments.isRepairerOfAnyKind(worker)) {
            return true;
        }

        return false;
    }

    // =========================================================
    /**
     * Assigns given worker unit (which is idle by now at least doesn't have anything to do) to gather
     * minerals.
     */
    private static void handleGatherMineralsOrGas(AUnit worker) {

        // Don't react if already gathering
        if (worker.isGatheringGas() || worker.isGatheringMinerals()) {
            worker.setTooltip("Gathering");
            return;
        }

        // If is carrying minerals, return
        if (worker.isCarryingGas() || worker.isCarryingMinerals()) {
            worker.returnCargo();
            worker.setTooltip("Cargo");
            return;
        }

        if (worker.isMoving() || worker.getTarget() != null) {
            return;
        }

        // If basically unit is not doing a shit, send it to gather resources (minerals or gas).
        // But check for multiple conditions (like if isn't constructing, repairing etc).
        if (worker.isIdle() || (!worker.isGatheringMinerals() && !worker.isGatheringGas() && !worker.isMoving()
                && !worker.isConstructing() && !worker.isAttacking() && !worker.isRepairing())) {
            worker.setTooltip("Move ass!");
            AMineralGathering.gatherResources(worker);
        }
    }

    // =========================================================
    // Auxiliary
    /**
     * Returns total number of workers that are currently assigned to this building.
     */
    public static int getHowManyWorkersWorkingNear(AUnit base, boolean includeMoving) {
//        boolean isGasBuilding = target.getType().isGasBuilding();
//        boolean isBase = target.isBase();
//        int total = 0;
//
//        for (AUnit worker : Select.ourWorkers().listUnits()) {
//            if (isWorkerAssignedToBuilding(worker, target)) {
//                total++;
//            }
//        }
//        return total;

        int total = 0;

        for (AUnit worker : Select.ourWorkers().inRadius(25, base).listUnits()) {
            if (worker.isMiningOrExtractingGas() || worker.isRepairing() || worker.isConstructing()
                    || (includeMoving && worker.isMoving())) {
                total++;
            }
        }

        return total;

//        return Select.ourWorkers().inRadius(15, base).count();
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

    public static int countWorkersAssignedTo(AUnit unit) {
        int count = 0;
        for (AUnit worker : Select.ourWorkers().list()) {
            if (unit.equals(worker.getTarget())) {
                count++;
            }
        }
        return count;
    }
}
