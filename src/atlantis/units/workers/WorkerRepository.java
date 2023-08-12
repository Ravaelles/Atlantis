package atlantis.units.workers;

import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class WorkerRepository {
    /**
     * Returns total number of units that are currently assigned to this building.
     */
    public static int getHowManyWorkersWorkingNear(AUnit base, boolean includeMoving) {
        int total = 0;

        for (AUnit unit : Select.ourWorkers().inRadius(25, base).list()) {
            if (unit.isMiningOrExtractingGas() || unit.isRepairing() || unit.isConstructing()
                || (includeMoving && unit.isMoving())) {
                total++;
            }
        }

        return total;
    }

    public static AUnit getRandomWorkerAssignedTo(AUnit target) {
//        boolean isGasBuilding = target.type().isGasBuilding();

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
        if (building.equals(worker.target()) || building.equals(worker.orderTarget())) return true;
        else if (building.equals(worker.buildUnit())) return true;
        else if (building.type().isGasBuilding()) {
            return worker.isGatheringGas() && worker.distTo(building) <= 10;
        }
        else if (building.isBase()) {
            if (worker.isGatheringMinerals() || worker.isCarryingMinerals()) {
                return true;
            }
            else return worker.target() != null && worker.target().type().isMineralField();
        }

        return false;
    }

    public static int countWorkersAssignedTo(AUnit otherUnit) {
        int count = 0;
        for (AUnit worker : Select.ourWorkers().list()) {
            if (otherUnit.equals(worker.target())) {
                count++;
            }
        }
        return count;
    }
}
