package atlantis.workers;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;
import atlantis.util.PositionUtil;

import java.util.Collection;

public class AWorkerTransferManager {

    /**
     * Every base should have similar number of workers, more or less.
     */
    public static void transferWorkersBetweenBasesIfNeeded() {

        // Don't run every frame
        if (!AGame.everyNthGameFrame(30)) {
            return;
        }

        Collection<AUnit> ourBases = Select.ourBases().listUnits();
        if (ourBases.size() <= 1) {
            return;
        }

        // =========================================================

        // Count ratios of workers / minerals for every base
        Units baseWorkersRatios = new Units();
        for (AUnit base : ourBases) {
            int numOfWorkersNearBase = AWorkerManager.getHowManyWorkersWorkingNear(base, false);
            int numOfMineralsNearBase = Select.minerals().inRadius(10, base).count();
            double workersToMineralsRatio = (double) numOfWorkersNearBase / (numOfMineralsNearBase + 0.1);
            baseWorkersRatios.setValueFor(base, workersToMineralsRatio);
        }

        // Take the base with lowest and highest worker ratio
        AUnit baseWithFewestWorkers = baseWorkersRatios.getUnitWithLowestValue();
        AUnit baseWithMostWorkers = baseWorkersRatios.getUnitWithHighestValue();

        if (baseWithFewestWorkers == null || baseWithMostWorkers == null) {
//            System.err.println("baseWithFewestWorkers = " + baseWithFewestWorkers);
//            System.err.println("baseWithMostWorkers = " + baseWithMostWorkers);
            return;
        }

        double fewestWorkersRatio = baseWorkersRatios.getValueFor(baseWithFewestWorkers);
        double mostWorkersRatio = baseWorkersRatios.getValueFor(baseWithMostWorkers);
        double workerRatioDiff = mostWorkersRatio - fewestWorkersRatio;

//        System.out.println("Fewest: " + baseWithFewestWorkers + " / " + fewestWorkersRatio);
//        System.out.println("Most: " + baseWithMostWorkers + " / " + mostWorkersRatio);
//        System.out.println();

        if (mostWorkersRatio < 1.7 || workerRatioDiff < 0.6 || baseWithMostWorkers.distanceTo(baseWithFewestWorkers) < 10) {
            return;
        }

        // === Perform worker transfer from base to base ========================================

        AUnit worker = Select.ourWorkersThatGather(true)
                .inRadius(20, baseWithMostWorkers)
                .nearestTo(baseWithFewestWorkers);
        if (worker != null) {
            transferWorkerTo(worker, baseWithFewestWorkers);
        }
    }

    private static void transferWorkerTo(AUnit worker, AUnit baseWithFewestWorkers) {
        if (worker.distanceTo(baseWithFewestWorkers.getPosition()) > 6) {
            worker.move(baseWithFewestWorkers.getPosition(), UnitActions.TRANSFER, "Transfer");
        } else if (worker.isMoving()) {
            worker.holdPosition("Transferred!");
        }
    }

}
