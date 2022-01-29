package atlantis.workers;

import atlantis.AGame;
import atlantis.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.Units;
import atlantis.units.actions.Actions;

import java.util.Collection;

public class AWorkerTransferManager {

    /**
     * Every base should have similar number of workers, more or less.
     */
    public static void transferWorkersBetweenBasesIfNeeded() {

        // Don't run every frame
        if (!AGame.everyNthGameFrame(GamePhase.isEarlyGame() ? 10 : 30)) {
            return;
        }

        Collection<AUnit> ourBases = Select.ourBases().list();
        if (ourBases.size() <= 1) {
            return;
        }

        // =========================================================

        // Count ratios of workers / minerals for every base
        Units baseWorkersRatios = new Units();
        for (AUnit base : ourBases) {
            if (
                    base.isLifted()
                    || base.lastUnderAttackLessThanAgo(30 * 30)
                    || Select.enemyCombatUnits().inRadius(14, base).isNotEmpty()
            ) {
                continue;
            }

            int numOfWorkersNearBase = AWorkerManager.getHowManyWorkersWorkingNear(base, false);
            int numOfMineralsNearBase = Select.minerals().inRadius(10, base).count();
            double workersToMineralsRatio = (double) numOfWorkersNearBase / (numOfMineralsNearBase + 0.1);
            baseWorkersRatios.addUnitWithValue(base, workersToMineralsRatio);
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

        if (mostWorkersRatio < 2.1 || workerRatioDiff < 1.0 || baseWithMostWorkers.distTo(baseWithFewestWorkers) < 8) {
            return;
        }

        System.out.println(
                "Fewest: " + baseWithFewestWorkers
                        + " (" + baseWorkersRatios.valueFor(baseWithFewestWorkers)
                        + ") / " + fewestWorkersRatio
        );
        System.out.println(
                "Most: " + baseWithMostWorkers
                        + " (" + baseWorkersRatios.valueFor(baseWithMostWorkers)
                        + ") / " + fewestWorkersRatio
        );
        System.out.println();

        // === Perform worker transfer from base to base ========================================

        AUnit worker = Select.ourWorkersThatGather(true)
                .inRadius(20, baseWithMostWorkers)
                .nearestTo(baseWithFewestWorkers);
//        System.out.println("transfer worker = " + worker);
        if (worker != null) {
            transferWorkerTo(worker, baseWithFewestWorkers);
        }
    }

    private static void transferWorkerTo(AUnit worker, AUnit baseWithFewestWorkers) {
        if (worker.distTo(baseWithFewestWorkers.position()) > 3) {
            worker.move(baseWithFewestWorkers.position(), Actions.TRANSFER, "Transfer", true);
        } else if (worker.isMoving()) {
            AMineralGathering.gatherResources(worker);
            worker.setTooltipTactical("Transferred!");
        }
    }

}
