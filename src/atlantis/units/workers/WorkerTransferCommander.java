package atlantis.units.workers;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

import java.util.Collection;

public class WorkerTransferCommander extends Commander {

    /**
     * Every base should have similar number of workers, more or less.
     */
    protected void handle() {
//        if (true) return;

        // Don't run every frame
        if (!AGame.everyNthGameFrame(GamePhase.isEarlyGame() ? 11 : 41)) {
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
                    || base.enemiesNear().combatUnits().inRadius(15, base).isNotEmpty()
            ) {
                continue;
            }

            int numOfWorkersNearBase = WorkerRepository.getHowManyWorkersWorkingNear(base, false);
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

        if (mostWorkersRatio < 2.0 || workerRatioDiff < 1.0 || baseWithMostWorkers.distTo(baseWithFewestWorkers) < 8) {
            return;
        }

//        System.out.println(
//                "Fewest: " + baseWithFewestWorkers
//                        + " (" + baseWorkersRatios.valueFor(baseWithFewestWorkers)
//                        + ") / " + fewestWorkersRatio
//        );
//        System.out.println(
//                "Most: " + baseWithMostWorkers
//                        + " (" + baseWorkersRatios.valueFor(baseWithMostWorkers)
//                        + ") / " + fewestWorkersRatio
//        );
//        System.out.println();

        // === Perform worker transfer from base to base ========================================

        AUnit worker = Select.ourWorkersThatGather(true)
            .inRadius(16, baseWithMostWorkers)
            .nearestTo(baseWithFewestWorkers);
//        System.out.println("transfer worker = " + worker);
        if (worker != null) {
            transferWorkerTo(worker, baseWithFewestWorkers);
        }
    }

    /**
     * We're issuing Gather command instead of Move, because then workers can bypass stacked combat units.
     */
    private void transferWorkerTo(AUnit worker, AUnit baseWithFewestWorkers) {
        AUnit mineral = Select.minerals().inRadius(10, baseWithFewestWorkers).first();

        if (mineral == null) return;

        if (worker.distTo(mineral) > 8) {
            worker.move(baseWithFewestWorkers.position(), Actions.TRANSFER, "Transfer", true);
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
