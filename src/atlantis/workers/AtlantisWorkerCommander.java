package atlantis.workers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.buildings.managers.AtlantisGasManager;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.missions.UnitMissions;
import atlantis.util.PositionUtil;
import java.util.Collection;


/**
 * Manages all worker (SCV, Probe, Drone) actions.
 */
public class AtlantisWorkerCommander {

    /**
     * Executed only once per frame.
     */
    public static void update() {
        AtlantisGasManager.handleGasBuildings();
        handleNumberOfWorkersNearBases();

        for (AUnit unit : Select.ourWorkers().listUnits()) {
            AtlantisWorkerManager.update(unit);
        }
    }

    // =========================================================
    
    public static boolean shouldTrainWorkers(boolean checkSupplyAndMinerals) {

        // Check MINERALS
        if (checkSupplyAndMinerals && AtlantisGame.getMinerals() < 50) {
            return false;
        }

        // Check FREE SUPPLY
        if (checkSupplyAndMinerals && AtlantisGame.getSupplyFree() == 0) {
            return false;
        }

        int workers = Select.ourWorkers().count();

        // Check if not TOO MANY WORKERS
        if (workers >= 27 * Select.ourBases().count()) {
            return false;
        }

        // Check if AUTO-PRODUCTION of WORKERS is active.
        if (workers < AtlantisConfig.AUTO_PRODUCE_WORKERS_MAX_WORKERS) {
            return true;
        }

        // Check if ALLOWED TO PRODUCE IN PRODUCTION QUEUE
//        if (!AtlantisGame.getBuildOrders().shouldProduceNow(AtlantisConfig.WORKER)) {
//            return false;
//        }
//        if (!AtlantisGame.getBuildOrders().getThingsToProduceRightNow(true).isEmpty()) {
//            return false;
//        }

        // // Check if not TOO MANY WORKERS
        // if (AtlantisUnitInformationManager.countOurWorkers() >= 27 * AtlantisUnitInformationManager.countOurBases())
        // {
        // return false;
        // }
        return false;
    }
    
    // =========================================================

    /**
     * Every base should have similar number of workers, more or less.
     */
    private static void handleNumberOfWorkersNearBases() {
        
        // Don't run every frame
        if (AtlantisGame.getTimeFrames() % 10 != 0) {
            return;
        }
        
        // =========================================================
        
        Collection<AUnit> ourBases = Select.ourBases().listUnits();
        if (ourBases.size() <= 1) {
            return;
        }
        
        // Count ratios of workers / minerals for every base
        Units baseWorkersRatios = new Units();
        for (AUnit ourBase : ourBases) {
            int numOfWorkersNearBase = Select.ourWorkersThatGather().inRadius(15, ourBase.getPosition()).count();
            int numOfMineralsNearBase = Select.minerals().inRadius(10, ourBase.getPosition()).count() + 1;
            if (numOfWorkersNearBase <= 2) {
                continue;
            }
            double workersToMineralsRatio = (double) numOfWorkersNearBase / numOfMineralsNearBase;
//            System.out.println(ourBase + " / work:" + numOfWorkersNearBase + " / miner:" +numOfMineralsNearBase + " / RATIO:" + workersToMineralsRatio);
            baseWorkersRatios.setValueFor(ourBase, workersToMineralsRatio);
        }
        
        // Take the base with lowest and highest worker ratio
        AUnit baseWithFewestWorkers = baseWorkersRatios.getUnitWithLowestValue();
        AUnit baseWithMostWorkers = baseWorkersRatios.getUnitWithHighestValue();
        
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
                PositionUtil.distanceTo(baseWithMostWorkers, baseWithFewestWorkers) < 10) {
            return;
        }
        
        // If the difference is "significant" transfer one worker from base to base
        if (baseWorkersRatios.getValueFor(baseWithMostWorkers) - 0.1 > 
                baseWorkersRatios.getValueFor(baseWithFewestWorkers)) {
            AUnit worker = (AUnit) Select.ourWorkersThatGather().inRadius(10, baseWithMostWorkers.getPosition()).first();
            if (worker != null) {
                worker.move(baseWithFewestWorkers.getPosition(), UnitMissions.MOVE);
            }
        }
    }

}
