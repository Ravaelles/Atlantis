package atlantis.buildings.managers;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.production.ProductionOrder;
import atlantis.production.orders.AProductionQueue;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.workers.AWorkerManager;

import java.util.ArrayList;
import java.util.Collection;

public class AGasManager {

    private static final int MAX_GAS_WORKERS_PER_BUILDING = 3;

    // =========================================================
    
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
        
        Collection<AUnit> gasBuildings = Select.ourBuildings().ofType(AtlantisConfig.GAS_BUILDING).listUnits();
        Collection<AUnit> workers = Select.ourWorkers().listUnits();
        
        // =========================================================
        
        for (AUnit gasBuilding : gasBuildings) {
            if (!gasBuilding.isCompleted()) {
                continue;
            }
            
            int numOfWorkersNearby = countWorkersGatheringGasNear(gasBuilding);
            int optimalNumOfGasWorkers = defineOptimalGasWorkers(gasBuilding);
//            System.out.println(optimalNumOfGasWorkers + " // " + numOfWorkersNearby);

            // Less workers gathering gas than optimal
            if (numOfWorkersNearby < optimalNumOfGasWorkers) {
                assignBestWorkerToGasBuilding(gasBuilding);
                break; // Only one worker per execution
            }
            
            // More workers than optimal
            else if (numOfWorkersNearby > optimalNumOfGasWorkers) {
                AUnit worker = AWorkerManager.getRandomWorkerAssignedTo(gasBuilding);
                if (worker != null && worker.isGatheringGas()) {
                    worker.stop("I'm fired!");
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
        if (worker.isCarryingGas() || worker.isCarryingMinerals()) {
            worker.returnCargo();
            worker.setTooltip("Cargo");
            return;
        }

        worker.gather(gasBuilding);
        worker.setTooltip("Gas");
    }

    private static int countWorkersGatheringGasNear(AUnit gasBuilding) {
        int total = 0;

        for (AUnit worker : Select.ourWorkers().inRadius(12, gasBuilding).listUnits()) {
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

    private static int defineOptimalGasWorkers(AUnit gasBuilding) {
//        if (AGame.hasGas(800)) {
//            return 1;
//        }

        if (Select.ourBases().inRadius(10, gasBuilding).isEmpty()) {
            return 0;
        }
        
        int totalGasNeeded = 0;
        ArrayList<ProductionOrder> nextOrders = AProductionQueue.getProductionQueueNext(
                1 + (AGame.timeSeconds() > 300 ? 2 : 0)
        );
        for (ProductionOrder order : nextOrders) {
            totalGasNeeded += order.getGasRequired();
        }

        if (Select.ourWorkers().count() >= 30) {
            return 3;
        }

        return (totalGasNeeded > 0 && !AGame.hasGas(totalGasNeeded) ? 3 : 1);
    }

}
