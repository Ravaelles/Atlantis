package atlantis.workers;

import atlantis.AtlantisGame;
import atlantis.buildings.managers.AtlantisGasManager;
import atlantis.wrappers.SelectUnits;
import atlantis.wrappers.Units;
import java.util.Collection;
import jnibwapi.Unit;

/**
 * Manages all worker (SCV, Probe, Drone) actions.
 */
public class AtlantisWorkerCommander {

    /**
     * Executed only once per frame.
     */
    public static void update() {
        handleGasBuildings();
        handleNumberOfWorkersNearBases();

        for (Unit unit : SelectUnits.ourWorkers().list()) {
            AtlantisWorkerManager.update(unit);
        }
    }

    // =========================================================
    
    /**
     * If any of our gas extracting buildings needs worker, it will assign exactly one worker per frame (until
     * no more needed).
     */
    private static void handleGasBuildings() {
        Unit gasBuildingNeedingWorker = AtlantisGasManager.getOneGasBuildingNeedingWorker();
        if (gasBuildingNeedingWorker != null) {
            Unit worker = SelectUnits.ourWorkers().gatheringMinerals(true).first();
            if (worker != null) {
                worker.gather(gasBuildingNeedingWorker, false);
            }
        }
    }

    /**
     * Every base should have similar number of workers, more or less.
     */
    private static void handleNumberOfWorkersNearBases() {
        
        // Don't run every frame
        if (AtlantisGame.getTimeFrames() % 10 != 0) {
            return;
        }
        
        // =========================================================
        
        Collection<Unit> ourBases = SelectUnits.ourBases().list();
        if (ourBases.size() <= 1) {
            return;
        }
        
        // Count ratios of workers / minerals for every base
        Units baseWorkersRatios = new Units();
        for (Unit ourBase : ourBases) {
            int numOfWorkersNearBase = SelectUnits.ourWorkersThatGather().inRadius(15, ourBase).count();
            int numOfMineralsNearBase = SelectUnits.minerals().inRadius(10, ourBase).count() + 1;
            if (numOfWorkersNearBase <= 2) {
                continue;
            }
            double workersToMineralsRatio = (double) numOfWorkersNearBase / numOfMineralsNearBase;
//            System.out.println(ourBase + " / work:" + numOfWorkersNearBase + " / miner:" +numOfMineralsNearBase + " / RATIO:" + workersToMineralsRatio);
            baseWorkersRatios.setValueFor(ourBase, workersToMineralsRatio);
        }
        
        // Take the base with lowest and highest worker ratio
        Unit baseWithFewestWorkers = baseWorkersRatios.getUnitWithLowestValue();
        Unit baseWithMostWorkers = baseWorkersRatios.getUnitWithHighestValue();
        
        if (baseWithFewestWorkers == null || baseWithMostWorkers == null) {
//            System.err.println("baseWithFewestWorkers = " + baseWithFewestWorkers);
//            System.err.println("baseWithMostWorkers = " + baseWithMostWorkers);
            return;
        }
        
//        System.out.println("Fewest: " + baseWithFewestWorkers + " / " + baseWorkersRatios.getValueFor(baseWithFewestWorkers));
//        System.out.println("Most: " + baseWithMostWorkers + " / " + baseWorkersRatios.getValueFor(baseWithMostWorkers));
//        System.out.println();
        
        // If there's only 120% as many workers as minerals OR bases are too close, don't transfer
        if (baseWorkersRatios.getValueFor(baseWithMostWorkers) <= 1.2 || 
                baseWithMostWorkers.distanceTo(baseWithFewestWorkers) < 10) {
            return;
        }
        
        // If the difference is "significant" transfer one worker from base to base
        if (baseWorkersRatios.getValueFor(baseWithMostWorkers) - 0.1 > 
                baseWorkersRatios.getValueFor(baseWithFewestWorkers)) {
            Unit worker = SelectUnits.ourWorkersThatGather().inRadius(10, baseWithMostWorkers).first();
            if (worker != null) {
                worker.move(baseWithFewestWorkers);
            }
        }
    }

}
