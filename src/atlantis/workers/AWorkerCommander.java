package atlantis.workers;

import atlantis.AGame;
import atlantis.buildings.managers.AGasManager;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;
import atlantis.util.CodeProfiler;
import atlantis.util.PositionUtil;
import java.util.Collection;

/**
 * Manages all worker (SCV, Probe, Drone) actions.
 */
public class AWorkerCommander {

    /**
     * Executed only once per frame.
     */
    public static void update() {
        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_WORKERS);

        // === Handle assigning workers to gas / bases ============================
        
        AGasManager.handleGasBuildings();
//        transferWorkersBetweenBasesIfNeeded(); // @FIX: Doesn't work and is buggy

        // === Act individually with every worker =================================

        for (AUnit worker : Select.ourWorkers().listUnits()) {
            AWorkerManager.update(worker);
        }
        
        // =========================================================
        
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_WORKERS);
    }

    // =========================================================
    
    /**
     * Every base should have similar number of workers, more or less.
     */
    private static void transferWorkersBetweenBasesIfNeeded() {

        // Don't run every frame
        if (AGame.getTimeFrames() % 3 != 0) {
            return;
        }

        // =========================================================
        Collection<AUnit> ourBases = Select.ourBases().listUnits();
        if (ourBases.size() <= 1) {
            return;
        }

        // Count ratios of workers / minerals for every base
        Units baseWorkersRatios = new Units();
//        System.out.println();
        for (AUnit base : ourBases) {
//            int numOfWorkersNearBase = Select.ourWorkersThatGather(false).inRadius(15, ourBase).count();
            int numOfWorkersNearBase = AWorkerManager.getHowManyWorkersGatheringAt(base);
            int numOfMineralsNearBase = Select.minerals().inRadius(10, base).count() + 1;
//            if (numOfWorkersNearBase <= 2) {
//                continue;
//            }
            double workersToMineralsRatio = (double) numOfWorkersNearBase / numOfMineralsNearBase;
//            System.out.println(ourBase + " / work:" + numOfWorkersNearBase + " / miner:" +numOfMineralsNearBase + " / RATIO:" + workersToMineralsRatio);
            baseWorkersRatios.setValueFor(base, workersToMineralsRatio);
//            System.out.println("getValueFor = " + baseWorkersRatios.getValueFor(ourBase));
        }

        // Take the base with lowest and highest worker ratio
        AUnit baseWithFewestWorkers = baseWorkersRatios.getUnitWithLowestValue();
        AUnit baseWithMostWorkers = baseWorkersRatios.getUnitWithHighestValue();

        if (baseWithFewestWorkers == null || baseWithMostWorkers == null) {
//            System.err.println("baseWithFewestWorkers = " + baseWithFewestWorkers);
//            System.err.println("baseWithMostWorkers = " + baseWithMostWorkers);
            return;
        }

        double fewestWorkers = baseWorkersRatios.getValueFor(baseWithFewestWorkers);
        double mostWorkers = baseWorkersRatios.getValueFor(baseWithMostWorkers);

//        System.out.println("Fewest: " + baseWithFewestWorkers + " / " + fewestWorkers);
//        System.out.println("Most: " + baseWithMostWorkers + " / " + mostWorkers);
//        System.out.println();

        // If there's only 117% as many workers as minerals OR bases are too close, don't transfer
        if (Math.abs(mostWorkers - fewestWorkers) < 0.17
                || PositionUtil.distanceTo(baseWithMostWorkers, baseWithFewestWorkers) < 6) {
            return;
        }
        
        // === Perform worker transfer from base to base ========================================

        AUnit worker = (AUnit) Select.ourWorkersThatGather(true)
                .inRadius(12, baseWithMostWorkers)
                .nearestTo(baseWithFewestWorkers);
        if (worker != null) {
            worker.move(baseWithFewestWorkers.getPosition(), UnitActions.MOVE);
//            System.err.println("Transfer from " + baseWithMostWorkers + " to " + baseWithFewestWorkers);
        }
    }

}
