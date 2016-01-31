package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.workers.AtlantisWorkerManager;
import atlantis.wrappers.SelectUnits;
import java.util.Collection;
import jnibwapi.Unit;
import jnibwapi.types.TechType;
import jnibwapi.types.UpgradeType;

public class AtlantisGasManager {

    private static final int MIN_GAS_WORKERS_PER_BUILDING = 3;
    private static final int MAX_GAS_WORKERS_PER_BUILDING = 3;

    // =========================================================
    
    /**
     * If any of our gas extracting buildings needs worker, it will assign exactly one worker per frame (until
     * no more needed).
     */
    public static void handleGasBuildings() {
        if (AtlantisGame.getTimeFrames() % 10 != 0) {
            return;
        }
        
        // =========================================================
        
        Collection<Unit> gasBuildings = SelectUnits.ourBuildings().ofType(AtlantisConfig.GAS_BUILDING).list();
        Collection<Unit> workers = SelectUnits.ourWorkers().list();
        
        // =========================================================
        
        int MIN_GAS_WORKERS_PER_BUILDING = defineMinGasWorkersPerBuilding();

        for (Unit gasBuilding : gasBuildings) {
            int numberOfWorkersAssigned = AtlantisWorkerManager.getHowManyWorkersAt(gasBuilding);
            
            // Assign when LOWER THAN MIN
            if (numberOfWorkersAssigned < MIN_GAS_WORKERS_PER_BUILDING) {
                Unit worker = getWorkerForGasBuilding(gasBuilding);
                if (worker != null) {
                    worker.gather(gasBuilding, false);
                }
                break;
            }
            
            // Deassign when MORE THAN MAX
            else if (numberOfWorkersAssigned > MAX_GAS_WORKERS_PER_BUILDING) {
                Unit worker = AtlantisWorkerManager.getRandomWorkerAssignedTo(gasBuilding);
                if (worker != null) {
                    worker.stop(false);
                }
                break;
            }
        }
        
        // =========================================================
        
//        Unit gasBuildingNeedingWorker = AtlantisGasManager.getOneGasBuildingNeedingWorker();
//        if (gasBuildingNeedingWorker != null) {
//            Unit worker = SelectUnits.ourWorkers().gatheringMinerals(true).first();
//            if (worker != null) {
//                worker.gather(gasBuildingNeedingWorker, false);
//            }
//        }
    }
    
    // =========================================================
    
    /**
     * Returns first gas extracting building that needs a worker (because has less than 3 units assigned) or
     * null if every gas building has 3 workers assigned.
     */
//    private static Unit getOneGasBuildingNeedingWorker() {
//        Collection<Unit> gasBuildings = SelectUnits.ourBuildings().ofType(AtlantisConfig.GAS_BUILDING).list();
//        Collection<Unit> workers = SelectUnits.ourWorkers().list();
//
//        for (Unit gasBuilding : gasBuildings) {
////            int numberOfWorkersAssigned = countWorkersAssignedTo(gasBuilding, workers);
//            int numberOfWorkersAssigned = AtlantisWorkerManager.getHowManyWorkersAt(gasBuilding);
//            if (numberOfWorkersAssigned < MIN_GAS_WORKERS_PER_BUILDING) {
//                return gasBuilding;
//            }
//        }
//
//        return null;
//    }

    private static Unit getWorkerForGasBuilding(Unit gasBuilding) {
        Unit worker = SelectUnits.ourWorkers().gatheringMinerals(true).first();
        return worker;
    }

    public static int defineMinGasWorkersPerBuilding() {
        int seconds = AtlantisGame.getTimeSeconds();
        
        if (seconds < 350) {
            return 1;
        }
        else if (seconds < 500) {
            return 2;
        }
        else {
            return 3;
        }
        
//        return Math.max(1, 
//                Math.min(MIN_GAS_WORKERS_PER_BUILDING, (int) ((AtlantisGame.getTimeSeconds() - 90) / 90))
//        );
    }

    // =========================================================
    /**
     * Returns number of workers that are assigned to gather gas in given building.
     */
//    private static int countWorkersAssignedTo(Unit gasBuilding, Collection<Unit> workers) {
//        int total = 0;
//        for (Unit worker : workers) {
////            if (worker.getTarget() != null && worker.getTarget().equals(gasBuilding)) {
//            if (gasBuilding.equals(worker.getTarget()) || gasBuilding.equals(worker.getOrderTarget())) {
//                total++;
//            }
//        }
//        return total;
//    }

}
