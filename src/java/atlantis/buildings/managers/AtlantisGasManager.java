package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.units.AUnit;
import atlantis.workers.AtlantisWorkerManager;
import atlantis.units.Select;
import java.util.Collection;

import bwapi.TechType;
import bwapi.UpgradeType;

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
        
        Collection<AUnit> gasBuildings = (Collection<AUnit>) Select.ourBuildings().ofType(AtlantisConfig.GAS_BUILDING).listUnits();
        Collection<AUnit> workers = Select.ourWorkers().listUnits();
        
        // =========================================================
        
        int MIN_GAS_WORKERS_PER_BUILDING = defineMinGasWorkersPerBuilding();

        for (AUnit gasBuilding : gasBuildings) {
            int numberOfWorkersAssigned = AtlantisWorkerManager.getHowManyWorkersAt(gasBuilding);
            
            // Assign when LOWER THAN MIN
            if (numberOfWorkersAssigned < MIN_GAS_WORKERS_PER_BUILDING) {
                AUnit worker = getWorkerForGasBuilding(gasBuilding);
                if (worker != null) {
                    worker.gather(gasBuilding);
                }
                break;
            }
            
            // Deassign when MORE THAN MAX
            else if (numberOfWorkersAssigned > MAX_GAS_WORKERS_PER_BUILDING) {
                AUnit worker = AtlantisWorkerManager.getRandomWorkerAssignedTo(gasBuilding);
                if (worker != null) {
                    worker.stop();
                }
                break;
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
        int seconds = AtlantisGame.getTimeSeconds();
        
        if (seconds < 150) {
            return 1;
        }
        else if (seconds < 200) {
            return 2;
        }
        else {
            return 3;
        }
    }

}
