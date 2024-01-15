package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.units.workers.GatherResources;
import atlantis.units.workers.WorkerRepository;
import atlantis.util.log.ErrorLog;

import java.util.Collection;

public class NumberOfGasWorkersCommander extends Commander {
    @Override
    public boolean applies() {
        return A.everyNthGameFrame(7);
    }

    @Override
    protected void handle() {
        Collection<AUnit> gasBuildings = Select.ourBuildings().ofType(AtlantisRaceConfig.GAS_BUILDING).list();

        for (AUnit gasBuilding : gasBuildings) {
            boolean noBaseIsNearThisGasBuilding = Select.ourBases().inRadius(12, gasBuilding).count() == 0;

            int realCount = CountGasWorkers.countWorkersGatheringGasFor(gasBuilding);
            int expectedCount = noBaseIsNearThisGasBuilding ? 0 : expectedGasWorkers(gasBuilding, realCount);

            // Fewer workers gathering gas than optimal
            if (realCount < expectedCount) {
                assignBestWorkerToGasBuilding(gasBuilding);
                break; // Only one worker per execution - prevent weird runs
            }

            // More workers than optimal
            else if (realCount > expectedCount) {
                AUnit worker = WorkerRepository.getRandomWorkerAssignedTo(gasBuilding);
                if (worker != null && worker.isGatheringGas()) {
//                    System.out.println("FIRE GAS WORKER = " + worker + " / " + worker.getLastCommand());
                    worker.stop("Fired!");
                    (new GatherResources(worker)).invoke(this);
                }
                break; // Only one worker per execution - prevent weird runs
            }
        }
    }

    // =========================================================

    public static int defineGasWorkersPerBuilding() {
        int workers = Count.workers();

//        if (workers <= 8) {
//            return 0;
//        }

        if (workers <= 7) {
            return 0;
        }

        if (workers <= 13 && !A.hasMinerals(150)) {
            return 1;
        }

        if (A.gas() >= 380 && A.minerals() <= 280) {
            return A.inRange(1, Count.workers() / 12, 3);
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

    // =========================================================
    private static void assignBestWorkerToGasBuilding(AUnit gasBuilding) {
        AUnit worker = getWorkerForGasBuilding(gasBuilding);
        if (worker == null) {
            if (Count.workers() >= 6) ErrorLog.printMaxOncePerMinute("No worker for gas building");
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
        return FreeWorkers.get().gatheringMinerals(true).nearestTo(gasBuilding);
    }

    private static int expectedGasWorkers(AUnit gasBuilding, int numOfWorkersNear) {
        return defineGasWorkersPerBuilding();
    }
}
