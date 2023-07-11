package atlantis.units.workers;

import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.game.A;
import atlantis.production.constructing.ABuilderManager;
import atlantis.production.constructing.AConstructionManager;
import atlantis.terran.repair.ARepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class AWorkerManager {

    /**
     * Executed for every worker unit.
     */
    public static boolean update(AUnit worker) {
//        if (ScoutManager.testRoamingAroundBase(worker)) return true;

        if (workerManagerForbiddenFor(worker)) {
            return false;
        }

        if (worker.enemiesNear().notEmpty()) {
            if (AvoidCriticalUnits.update(worker)) {
                return true;
            }

            worker.setTooltipTactical(":)");

            if (AWorkerDefenceManager.handleDefenceIfNeeded(worker)) {
                return true;
            }

            if (AvoidEnemies.avoidEnemiesIfNeeded(worker)) {
                return true;
            }
        }

//        // Act as BUILDER
        if (AConstructionManager.isBuilder(worker)) {
            worker.setTooltipTactical("Builder");
            return ABuilderManager.update(worker);
        }

        if (worker.idIsEven() && handleDynamicRepaisNearby(worker)) {
            return true;
        }

        // Ordinary WORKER
        worker.setTooltipTactical("Gather");
        return handleGatherMineralsOrGas(worker);
    }

    // =========================================================

    private static boolean workerManagerForbiddenFor(AUnit worker) {
        if (worker.isScout()) {
            worker.setTooltipTactical("Scout");
            return true;
        }

        if (ARepairAssignments.isRepairerOfAnyKind(worker)) {
            return true;
        }

        return false;
    }

    // =========================================================

    /**
     * Assigns given worker unit (which is idle by now at least doesn't have anything to do) to gather minerals.
     */
    private static boolean handleGatherMineralsOrGas(AUnit worker) {

        // Don't react if already gathering
        // @Check Surprisingly, isMiningOrExtractingGas is quite slow! looksIdle works faster
        if (!worker.looksIdle()) {
            worker.setTooltipTactical("Miner");
            return true;
        }

        if (worker.isRepairing()) {
            worker.setTooltipTactical("Repair");
            return true;
        }

        // If basically unit is not doing a shit, send it to gather resources (minerals or gas).
        // But check for multiple conditions (like if isn't constructing, repairing etc).
        if (worker.isIdle() || (!worker.isGatheringMinerals() && !worker.isGatheringGas() && !worker.isMoving()
                && !worker.isConstructing() && !worker.isAttackingOrMovingToAttack() && !worker.isRepairing())) {
            worker.setTooltipTactical("Move ass!");
            return AMineralGathering.gatherResources(worker);
        }

        return true;
    }

    // =========================================================
    // Auxiliary
    /**
     * Returns total number of workers that are currently assigned to this building.
     */
    public static int getHowManyWorkersWorkingNear(AUnit base, boolean includeMoving) {
        int total = 0;

        for (AUnit worker : Select.ourWorkers().inRadius(25, base).list()) {
            if (worker.isMiningOrExtractingGas() || worker.isRepairing() || worker.isConstructing()
                    || (includeMoving && worker.isMoving())) {
                total++;
            }
        }

        return total;
    }

    public static AUnit getRandomWorkerAssignedTo(AUnit target) {
        boolean isGasBuilding = target.type().isGasBuilding();

        // Take those not carrying anything first
        for (AUnit worker : Select.ourWorkers().list()) {
            if (!isWorkerAssignedToBuilding(worker, target)) {
                continue;
            }

            if (!worker.isCarryingGas() && !worker.isCarryingMinerals()) {
                return worker;
            }
        }

        // Meh, take those carrying as well
        for (AUnit worker : Select.ourWorkers().list()) {
            if (isWorkerAssignedToBuilding(worker, target)) {
                return worker;
            }
        }

        return null;
    }
    
    public static boolean isWorkerAssignedToBuilding(AUnit worker, AUnit building) {
        if (building.equals(worker.target()) || building.equals(worker.orderTarget())) {
            return true;
        } else if (building.equals(worker.buildUnit())) {
            return true;
        } 
        else if (building.type().isGasBuilding()) {
            return worker.isGatheringGas() && worker.distTo(building) <= 10;
        }
        else if (building.isBase()) {
            if (worker.isGatheringMinerals() || worker.isCarryingMinerals()) {
                return true;
            } else return worker.target() != null && worker.target().type().isMineralField();
        }
        
        return false;
    }

    public static int countWorkersAssignedTo(AUnit unit) {
        int count = 0;
        for (AUnit worker : Select.ourWorkers().list()) {
            if (unit.equals(worker.target())) {
                count++;
            }
        }
        return count;
    }

    private static boolean handleDynamicRepaisNearby(AUnit worker) {
        if (!A.hasMinerals(15)) {
            return false;
        }

        AUnit repairable = worker.friendsNear().mechanical().wounded().inRadius(4, worker).nearestTo(worker);

        if (repairable != null && repairable.isWalkable()) {
            worker.repair(repairable, "KindGuy", false);
            return true;
        }

        return false;
    }
}
