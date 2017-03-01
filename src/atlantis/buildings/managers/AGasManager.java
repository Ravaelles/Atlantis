package atlantis.buildings.managers;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.workers.AWorkerManager;
import bwapi.Color;
import java.util.Collection;

public class AGasManager {

    private static final int MAX_GAS_WORKERS_PER_BUILDING = 3;

    // =========================================================
    
    /**
     * If any of our gas extracting buildings needs worker, it will assign exactly one worker per frame (until
     * no more needed).
     */
    public static void handleGasBuildings() {
        
        // Only once per second
        if (AGame.getTimeFrames() % 5 != 0) {
            return;
        }
        int minGasWorkersPerBuilding = defineMinGasWorkersPerBuilding();
        
        // =========================================================
        
        Collection<AUnit> gasBuildings = (Collection<AUnit>) 
                Select.ourBuildings().ofType(AtlantisConfig.GAS_BUILDING).listUnits();
        Collection<AUnit> workers = Select.ourWorkers().listUnits();
        
        // =========================================================
        
        for (AUnit gasBuilding : gasBuildings) {
            if (!gasBuilding.isCompleted()) {
                continue;
            }
            
            int numberOfWorkersAssigned = AWorkerManager.getHowManyWorkersGatheringAt(gasBuilding);
            APainter.paintTextCentered(gasBuilding, "" + numberOfWorkersAssigned, Color.Green);
            
            // Assign when LOWER THAN MIN
            if (numberOfWorkersAssigned < minGasWorkersPerBuilding) {
                AUnit worker = getWorkerForGasBuilding(gasBuilding);
                if (worker != null) {
                    worker.gather(gasBuilding);
                }
                break; // Only one worker per call
            }
            
            // Deassign when MORE THAN MAX
            else if (numberOfWorkersAssigned > MAX_GAS_WORKERS_PER_BUILDING) {
                AUnit worker = AWorkerManager.getRandomWorkerAssignedTo(gasBuilding);
                if (worker != null) {
                    worker.stop();
                }
                break; // Only one worker per call
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

}
