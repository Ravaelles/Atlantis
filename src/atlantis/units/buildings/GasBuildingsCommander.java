package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.WorkerRepository;

import java.util.Collection;

public class GasBuildingsCommander extends Commander {

    /**
     * If any of our gas extracting buildings needs worker, it will assign exactly one worker per frame (until
     * no more needed).
     */
    @Override
    protected void handle() {
        if (AGame.notNthGameFrame(9)) {
            return;
        }

        controlNumberOfWorkersGatheringGasAtEachBuilding();

        if (tooEarlyForAnotherGasBuilding()) {
            return;
        }

//        if (Count.inProductionOrInQueue(AtlantisRaceConfig.GAS_BUILDING) >= 1) {
//            return;
//        }
    }

    private static void controlNumberOfWorkersGatheringGasAtEachBuilding() {
        Collection<AUnit> gasBuildings = Select.ourBuildings().ofType(AtlantisRaceConfig.GAS_BUILDING).list();

        for (AUnit gasBuilding : gasBuildings) {
//            if (!gasBuilding.isCompleted()) {
//                continue;
//            }

            int realCount = CountGasWorkers.countWorkersGatheringGasFor(gasBuilding);
            int expectedCount = expectedGasWorkers(gasBuilding, realCount);


            // Fewer workers gathering gas than optimal
            if (realCount < expectedCount) {
                assignBestWorkerToGasBuilding(gasBuilding);
                break; // Only one worker per execution - prevent weird runs
            }

            // More workers than optimal
            else if (realCount > expectedCount) {
                AUnit worker = WorkerRepository.getRandomWorkerAssignedTo(gasBuilding);
                if (worker != null && worker.isGatheringGas()) {
                    worker.stop("I'm fired!", true);
                }
                break; // Only one worker per execution - prevent weird runs
            }
        }
    }

    // =========================================================

    private static boolean tooEarlyForAnotherGasBuilding() {
        if (Count.existingOrInProduction(AtlantisRaceConfig.GAS_BUILDING) >= 1) {
            if (!A.hasMinerals(200) || A.supplyTotal() <= 30) {
                return true;
            }
        }

        return false;
    }

    private static void assignBestWorkerToGasBuilding(AUnit gasBuilding) {
        AUnit worker = getWorkerForGasBuilding(gasBuilding);
        if (worker == null) {
            return;
        }

        // If is carrying stuff, return first
//        if (worker.isCarryingGas() || worker.isCarryingMinerals()) {
//            worker.returnCargo();
//            worker.setTooltip("Cargo");
//

//            return;
//        }

        worker.gather(gasBuilding);
        worker.setTooltipTactical("Gas");
    }

    private static AUnit getWorkerForGasBuilding(AUnit gasBuilding) {
        return Select.ourWorkers().gatheringMinerals(true).nearestTo(gasBuilding);
    }

    public static int defineGasWorkersPerBuilding() {
//        return 3;

        int workers = Count.workers();

        if (workers <= 8) {
            return 0;
        }

        if (workers <= 13 && !A.hasMinerals(150)) {
            return 1;
        }

        int seconds = A.seconds();

        if (seconds < 150 && A.hasGas(170)) {
            return 1;
        }
        else if (seconds < 250 && A.hasGas(250)) {
            return 2;
        }
        else {
            return 3;
        }
    }

    private static int expectedGasWorkers(AUnit gasBuilding, int numOfWorkersNear) {
        return defineGasWorkersPerBuilding();
    }
}
