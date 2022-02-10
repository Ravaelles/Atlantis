package atlantis.units.buildings;

import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.AWorkerManager;

import java.util.Collection;

public class AGasManager {

    /**
     * If any of our gas extracting buildings needs worker, it will assign exactly one worker per frame (until
     * no more needed).
     */
    public static void handleGasBuildings() {
        if (AGame.notNthGameFrame(7)) {
            return;
        }
//        int minGasWorkersPerBuilding = defineMinGasWorkersPerBuilding();
        
        // =========================================================
        
        Collection<AUnit> gasBuildings = Select.ourBuildings().ofType(AtlantisConfig.GAS_BUILDING).list();
//        Collection<AUnit> workers = Select.ourWorkers().listUnits();
        
        // =========================================================
        
        for (AUnit gasBuilding : gasBuildings) {
            if (!gasBuilding.isCompleted()) {
                continue;
            }
            
            int numOfWorkersNear = countWorkersGatheringGasNear(gasBuilding);
            int optimalNumOfGasWorkers = gasWorkers(gasBuilding, numOfWorkersNear);
//            System.out.println(optimalNumOfGasWorkers + " // " + numOfWorkersNear);

            // Less workers gathering gas than optimal
            if (numOfWorkersNear < optimalNumOfGasWorkers) {
                assignBestWorkerToGasBuilding(gasBuilding);
                break; // Only one worker per execution
            }
            
            // More workers than optimal
            else if (numOfWorkersNear > optimalNumOfGasWorkers) {
                AUnit worker = AWorkerManager.getRandomWorkerAssignedTo(gasBuilding);
                if (worker != null && worker.isGatheringGas()) {
                    worker.stop("I'm fired!", true);
                }
                break; // Only one worker per execution
            }
        }
        
        // =========================================================
        
//        AUnit gasBuildingNeedingWorker = AtlantisGasManager.getOneGasBuildingNeedingWorker();
//        if (gasBuildingNeedingWorker != null) {
//            AUnit worker = Select.ourWorkers().gatheringMinerals(true).first();
//            if (worker != null) {
//                worker.gather(gasBuildingNeedingWorker, false);
//            }
//        }
    }

    // =========================================================

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

    public static int defineMinGasWorkersPerBuilding() {
        return 3;
        
//        int seconds = AGame.getTimeSeconds();
//        
//        if (seconds < 150) {
//            return 1;
//        }
//        else if (seconds < 200) {
//            return 2;
//        }
//        else {
//            return 3;
//        }
    }

    private static int gasWorkers(AUnit gasBuilding, int numOfWorkersNear) {

        if (Count.workers() <= 8) {
            return 0;
        }

        // Too much gas, too little minerals
        if (A.hasGas(400) || Count.workers() <= 16 || (!A.hasMinerals(160) && A.hasGas(150))) {
            return 1;
        }

//        if (AGame.hasGas(800)) {
//            return 1;
//        }

//        if (gasBuilding.isDepleted()) {
//            return GamePhase.isLateGame() && AGame.canAfford(0, 200) ? 0 : 1;
//        }

//        if (Select.ourBases().inRadius(10, gasBuilding).isEmpty()) {
//            return 0;
//        }

        if (GamePhase.isEarlyGame() && AGame.canAfford(0, 100)) {
            return (int) A.inRange(1, numOfWorkersNear / 4, 3);
        }

        return 3;
        
//        int totalGasNeeded = 0;
//        ArrayList<ProductionOrder> nextOrders = ProductionQueue.nextInProductionQueue(
//                1 + (AGame.timeSeconds() > 300 ? 2 : 0)
//        );
//        for (ProductionOrder order : nextOrders) {
//            totalGasNeeded += order.getGasRequired();
//        }
//
//        if (Select.ourWorkers().count() >= 30) {
//            return 3;
//        }
//
//        return (totalGasNeeded > 0 && !AGame.hasGas(totalGasNeeded) ? 3 : 1);
    }

}
