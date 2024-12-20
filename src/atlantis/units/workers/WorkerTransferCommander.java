package atlantis.units.workers;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.information.generic.OurArmy;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Select;

import java.util.Collection;

public class WorkerTransferCommander extends Commander {
    private int maxAtOnceBonus = 8;

    /**
     * Every base should have similar number of workers, more or less.
     */
    protected void handle() {
//        if (true) return;

        // Don't run every frame
        if (!AGame.everyNthGameFrame(onceEveryFrames())) {
            return;
        }

        Collection<AUnit> ourBases = Select.ourBases().list();
        if (ourBases.size() <= 1) {
            return;
        }

        // =========================================================

        boolean result = false;
        for (int i = 0; i < maxAtOnceBonus; i++) {
            result = doTheTransferIfNeeded(ourBases) || result;
        }

        if (result) maxAtOnceBonus = 1;
    }

    private static int onceEveryFrames() {
        return AGame.supplyUsed() >= 90 ? 23 : 41;
    }

    private boolean doTheTransferIfNeeded(Collection<AUnit> ourBases) {
        // Count ratios of workers / minerals for every base
        Units baseWorkersRatios = new Units();
        for (AUnit base : ourBases) {
            if (
                base.isLifted()
                    || base.lastUnderAttackLessThanAgo(30 * 10)
                    || base.enemiesNear().combatUnits().inRadius(10, base).atLeast(1)
            ) {
                continue;
            }

            int numOfWorkersNearBase = WorkerRepository.countWorkersHarvestingMineralsNear(base, false);
            int numOfMineralsNearBase = Select.minerals().inRadius(10, base).count();
            double workersToMineralsRatio = (double) numOfWorkersNearBase / (numOfMineralsNearBase + 0.1);
            baseWorkersRatios.addUnitWithValue(base, workersToMineralsRatio);
        }

        if (baseWorkersRatios.size() <= 1) return false;

        // Take the base with lowest and highest worker ratio
        AUnit baseWithFewestWorkers = baseWorkersRatios.unitWithLowestValue();
        AUnit baseWithMostWorkers = baseWorkersRatios.unitWithHighestValue();

        if (baseWithFewestWorkers == null || baseWithMostWorkers == null) {
//            System.err.println("baseWithFewestWorkers = " + baseWithFewestWorkers);
//            System.err.println("baseWithMostWorkers = " + baseWithMostWorkers);
            return false;
        }

        double fewestWorkersRatio = baseWorkersRatios.valueFor(baseWithFewestWorkers);
        double mostWorkersRatio = baseWorkersRatios.valueFor(baseWithMostWorkers);
        double workerRatioDiff = mostWorkersRatio - fewestWorkersRatio;

        if (mostWorkersRatio < 2.0 || workerRatioDiff < 1.0 || baseWithMostWorkers.distTo(baseWithFewestWorkers) < 8) {
            return false;
        }

//        System.err.println(
//            "Fewest: " + baseWithFewestWorkers
//                + " (" + baseWorkersRatios.valueFor(baseWithFewestWorkers)
//                + ") / " + fewestWorkersRatio
//        );
//
//        System.err.println(
//            "Most: " + baseWithMostWorkers
//                + " (" + baseWorkersRatios.valueFor(baseWithMostWorkers)
//                + ") / " + fewestWorkersRatio
//        );


        // === Perform worker transfer from base to base ========================================

        return handleTransferWorkers(baseWithMostWorkers, baseWithFewestWorkers, workerRatioDiff);
    }

    private boolean handleTransferWorkers(AUnit baseWithMostWorkers, AUnit baseWithFewestWorkers, double workerRatioDiff) {
        boolean result = false;
        int n = workerRatioDiff > 2.0 ? 6 : 2;

        for (int i = 0; i < n; i++) {
            AUnit worker = Select.ourWorkersMiningMinerals(true)
                .inRadius(15, baseWithMostWorkers)
                .groundNearestTo(baseWithFewestWorkers);

//        System.err.println(
//            "transfer worker = " + worker + " to " + baseWithFewestWorkers + " dist:"
//                + baseWithFewestWorkers.distTo(worker)
//        );

            if (worker != null) {
                transferWorkerTo(worker, baseWithFewestWorkers);
                result = true;
            }
        }

        return result;
    }

    /**
     * We're issuing Gather command instead of Move, because then workers can bypass stacked combat units.
     */
    private void transferWorkerTo(AUnit worker, AUnit baseWithFewestWorkers) {
        AUnit mineral = Select.minerals().inRadius(10, baseWithFewestWorkers).first();
//        System.err.println("mineral = " + mineral);

        if (mineral == null) return;

        if (worker.distTo(mineral) > 8) {
//            worker.move(baseWithFewestWorkers.position(), Actions.TRANSFER, "Transfer", true);
            worker.gather(mineral);
            worker.setTooltip("Transfer");
        }
        else {
            AMineralGathering.gatherResources(worker);
            worker.setTooltipTactical("Transferred!");
        }

//        if (worker.distTo(baseWithFewestWorkers.position()) > 5) {
//            worker.move(baseWithFewestWorkers.position(), Actions.TRANSFER, "Transfer", true);
//        }
//        else if (worker.isMoving()) {
//            AMineralGathering.gatherResources(worker);
//            worker.setTooltipTactical("Transferred!");
//        }
    }

}
