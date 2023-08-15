package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisConfig;
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

        if (tooEarlyForAnotherGasBuilding()) {
            return;
        }

//        if (ConstructionRequests.countNotFinishedOfType(Terran_Factory) >= 1) {
        if (Count.inProductionOrInQueue(AtlantisConfig.GAS_BUILDING) >= 1) {
            return;
        }

//        System.out.println("@ " + A.now() + " - GAS - " + Count.inProductionOrInQueue(AtlantisConfig.GAS_BUILDING) +
//            " / " + AtlantisConfig.GAS_BUILDING);

        // =========================================================

        Collection<AUnit> gasBuildings = Select.ourBuildings().ofType(AtlantisConfig.GAS_BUILDING).list();

        // =========================================================

        for (AUnit gasBuilding : gasBuildings) {
            if (!gasBuilding.isCompleted()) {
                continue;
            }

            int realCount = countWorkersGatheringGasNear(gasBuilding);
            int expectedCount = expectedGasWorkers(gasBuilding, realCount);
//            System.out.println("OPTIMAL_GAS=" + expectedCount + " // realCount=" + realCount);

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
        if (Count.existingOrInProduction(AtlantisConfig.GAS_BUILDING) >= 1) {
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
//            System.out.println("ret cargo");
//            return;
//        }

        worker.gather(gasBuilding);
        worker.setTooltipTactical("Gas");
    }

    private static int countWorkersGatheringGasNear(AUnit gasBuilding) {
        int total = 0;

        for (AUnit worker : Select.ourWorkers().inRadius(12, gasBuilding).list()) {
            if (worker.isGatheringGas()) {
                total++;
            }
        }

        return total;
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

        if (workers <= 15 && !A.hasMinerals(150)) {
            return 1;
        }

        int seconds = A.seconds();

        if (seconds < 150 && A.hasGas(100)) {
            return 1;
        }
        else if (seconds < 250 && A.hasGas(100)) {
            return 2;
        }
        else {
            return 3;
        }
    }

    private static int expectedGasWorkers(AUnit gasBuilding, int numOfWorkersNear) {
        return defineGasWorkersPerBuilding();

//        if (Count.workers() <= 8) {
//            return 0;
//        }
//
//        // Too much gas, too little minerals
//        if (A.seconds() <= 800) {
//            if ((A.hasGas(170) && Count.workers() <= 19) || (!A.hasMinerals(160) && A.hasGas(150))) {
//                return 1;
//            }
//        }
//
////        if (AGame.hasGas(800)) {
////            return 1;
////        }
//
////        if (gasBuilding.isDepleted()) {
////            return GamePhase.isLateGame() && AGame.canAfford(0, 200) ? 0 : 1;
////        }
//
////        if (Select.ourBases().inRadius(10, gasBuilding).isEmpty()) {
////            return 0;
////        }
//
//        if (GamePhase.isEarlyGame() && AGame.canAfford(0, 100)) {
//            return (int) A.inRange(1, numOfWorkersNear / 4, 3);
//        }
//
//        return 3;
//
////        int totalGasNeeded = 0;
////        ArrayList<ProductionOrder> nextOrders = ProductionQueue.nextInProductionQueue(
////                1 + (AGame.timeSeconds() > 300 ? 2 : 0)
////        );
////        for (ProductionOrder order : nextOrders) {
////            totalGasNeeded += order.getGasRequired();
////        }
////
////        if (Select.ourWorkers().count() >= 30) {
////            return 3;
////        }
////
////        return (totalGasNeeded > 0 && !AGame.hasGas(totalGasNeeded) ? 3 : 1);
    }

}
