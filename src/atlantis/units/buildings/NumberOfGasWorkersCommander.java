package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.units.workers.GatherResources;
import atlantis.units.workers.WorkerRepository;
import atlantis.util.We;

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

    public static int defineGasWorkersPerBuilding() {
        int workers = Count.workers();
        int gas = A.gas();

//        if (workers <= 8) {
//            return 0;
//        }

        if (workers <= 7) {
            return 0;
        }

        if (workers >= 30 && gas <= 450) {
            return 3;
        }

        if (workers >= 25 && gas <= 350) {
            return 3;
        }

        if (We.protoss()) {
            if (A.s <= 500 && gas >= 80) return 1;
            if (A.s <= 600 && gas >= 120) return 2;
            if (A.s <= 700 && gas >= 180) return 1;
        }

        if (gas >= 410) {
            return 1;
        }
        else if (gas >= 300) {
            if (A.seconds() <= 400) {
                if (A.minerals() <= 400) return 0;
                return 1;
            }
            else {
                if (A.minerals() <= 600) return 2;
            }
        }

        if (workers <= 13 && !A.hasMinerals(150)) {
            return 1;
        }

        if (A.hasGas(240)) return 2;
        if (A.hasGas(310)) return 1;

        if (A.seconds() <= 900) {
            if (workers <= 23 && A.hasGas(270)) {
                return 2;
            }
            if (workers <= 30 && A.hasGas(350)) {
                return 2;
            }
        }

        if (gas >= 380 && A.minerals() <= 280) {
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
            .inRadius(15, gasBuilding)
            .gatheringMinerals(true)
            .nearestTo(gasBuilding);
    }

    private static int expectedGasWorkers() {
        return defineGasWorkersPerBuilding();
    }
}
