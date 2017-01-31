package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.debug.AtlantisPainter;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.workers.AtlantisWorkerManager;
import bwapi.Color;
import java.util.Collection;

public class AtlantisGasManager {

    private static final int MAX_GAS_WORKERS_PER_BUILDING = 3;

    // =========================================================
    
    /**
     * If any of our gas extracting buildings needs worker, it will assign exactly one worker per frame (until
     * no more needed).
     */
    public static void handleGasBuildings() {
        
        // Only once per second
//        if (AtlantisGame.getTimeFrames() % 5 != 0) {
//            return;
//        }
        int minGasWorkersPerBuilding = defineMinGasWorkersPerBuilding();
        
        // =========================================================
        
        Collection<AUnit> gasBuildings = (Collection<AUnit>) 
                Select.ourBuildings().ofType(AtlantisConfig.GAS_BUILDING).listUnits();
        Collection<AUnit> workers = Select.ourWorkers().listUnits();
        
        // =========================================================
        
        for (AUnit gasBuilding : gasBuildings) {
            int numberOfWorkersAssigned = AtlantisWorkerManager.getHowManyWorkersAt(gasBuilding);
            AtlantisPainter.paintTextCentered(gasBuilding, "" + numberOfWorkersAssigned, Color.Green);
            
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
                AUnit worker = AtlantisWorkerManager.getRandomWorkerAssignedTo(gasBuilding);
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
        AUnit worker = Select.ourWorkers().gatheringMinerals(true).first();
        return worker;
    }

    public static int defineMinGasWorkersPerBuilding() {
        return 3;
        
//        int seconds = AtlantisGame.getTimeSeconds();
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
