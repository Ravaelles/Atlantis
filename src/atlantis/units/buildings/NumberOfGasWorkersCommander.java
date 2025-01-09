package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.units.workers.GatherResources;
import atlantis.units.workers.WorkerRepository;

import java.util.Collection;

public class NumberOfGasWorkersCommander extends Commander {
    @Override
    public boolean applies() {
        return A.everyNthGameFrame(9);
    }

    @Override
    protected void handle() {
        Collection<AUnit> gasBuildings = Select.ourBuildings().ofType(AtlantisRaceConfig.GAS_BUILDING).list();
        int expectedGasWorkers = expectedGasWorkers();

        for (AUnit gasBuilding : gasBuildings) {
            boolean shouldHaveNoWorkersAssigned = shouldHaveNoWorkersAssigned(gasBuilding);

            int realCount = CountGasWorkers.countWorkersGatheringGasFor(gasBuilding);
            int expectedCount = shouldHaveNoWorkersAssigned ? 0 : expectedGasWorkers;

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
//                    worker.stop("Fired!");
//                    (new GatherResources(worker)).invokeFrom(this);

                    // Send worker to the last base
                    AUnit lastBase = Select.ourBases().last();
                    if (lastBase != null) {
                        worker.move(lastBase, Actions.MOVE_TRANSFER, "FiredFromGas&Transfer");
                    }
                    else {
                        worker.setTooltip("FiredFromGas");
                        (new GatherResources(worker)).invokeFrom(this);
                    }
                }
                break; // Only one worker per execution - prevent weird runs
            }
        }
    }

    private static boolean shouldHaveNoWorkersAssigned(AUnit gasBuilding) {
        return Select.ourBases().inRadius(12, gasBuilding).count() == 0
            || gasBuilding.u().getResources() <= 1;
    }

    // =========================================================

    // =========================================================
    private static void assignBestWorkerToGasBuilding(AUnit gasBuilding) {
        AUnit worker = getWorkerForGasBuilding(gasBuilding);
        if (worker == null) {
//            if (Count.workers() >= 8) ErrorLog.printMaxOncePerMinute("No worker for gas building");
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
        return FreeWorkers.get()
            .inRadius(AUnit.NEAR_DIST, gasBuilding)
            .gatheringMinerals(true)
            .nearestTo(gasBuilding);
    }

    private static int expectedGasWorkers() {
        return GasWorkersPerBuilding.define();
    }
}
