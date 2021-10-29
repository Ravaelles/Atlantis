package atlantis.workers;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;

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
        AUnit baseWithFewestWorkers = baseWorkersRatios.unitWithLowestValue();
        AUnit baseWithMostWorkers = baseWorkersRatios.unitWithHighestValue();

        if (baseWithFewestWorkers == null || baseWithMostWorkers == null) {
//            System.err.println("baseWithFewestWorkers = " + baseWithFewestWorkers);
//            System.err.println("baseWithMostWorkers = " + baseWithMostWorkers);
            return;
        }

        double fewestWorkersRatio = baseWorkersRatios.valueFor(baseWithFewestWorkers);
        double mostWorkersRatio = baseWorkersRatios.valueFor(baseWithMostWorkers);
        double workerRatioDiff = mostWorkersRatio - fewestWorkersRatio;

//        System.out.println("Fewest: " + baseWithFewestWorkers + " / " + fewestWorkersRatio);
//        System.out.println("Most: " + baseWithMostWorkers + " / " + mostWorkersRatio);
//        System.out.println();

        if (mostWorkersRatio < 1.7 || workerRatioDiff < 0.6 || baseWithMostWorkers.distTo(baseWithFewestWorkers) < 10) {
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
        if (worker.distTo(baseWithFewestWorkers.position()) > 6) {
            worker.move(baseWithFewestWorkers.position(), UnitActions.TRANSFER, "Transfer");
        } else if (worker.isMoving()) {
            worker.holdPosition("Transferred!");
        }
    }

}
