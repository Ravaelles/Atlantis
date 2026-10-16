package atlantis.production.dynamic.workers;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.decisions.board.BoardState;
import atlantis.game.A;
import atlantis.information.strategy.Strategy;
import atlantis.production.dynamic.protoss.prioritize.ProtossCriticalStuffInQueue;
import atlantis.production.GoalBoardAdvisor;
import atlantis.production.GoalBoardDriver;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.PriorityWaiters;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.SoonInQueue;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.production.requests.produce.ProduceWorker;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;
import atlantis.util.We;

import static atlantis.units.AUnitType.*;

public class AutoProduceWorkersCommander extends Commander {
    public static String REASON = "";

    /**
     * Queue the next worker while the current one still trains (owner
     * 2026-10-14: 15 of ~300 Probe frames, so production never gaps).
     * Pinned by WorkerPumpEarlyGameTest.
     */
    public static final int NEXT_WORKER_WHEN_TRAINING_REMAINS = 15;

    /**
     * Selects the least worker-saturated base to build a worker.
     *
     * @return
     */
    @Override
    protected boolean handle() {
        if (!shouldProduceWorkers()) {
            // PROBE (NEXT.md #53): a run ends with Probe=15 after 10 game-minutes, which is the
            // shape of a worker-gate that never opens - and the file already carries a
            // documented case of it (line 54's 250-mineral gate). `REASON` was written and read
            // nowhere, so the refusing gate was invisible. One row per second under
            // BUILDER_LOG=1 turns "the economy is slow" into a named gate.
            // JSON-only since 2026-10-15 (owner: no red spam in the game log).
            if (atlantis.config.env.Env.builderLog() && A.everyNthGameFrame(30)) {
                EventLog.key(LogKey.WORKER_GATE)
                    .param("reason", REASON)
                    .param("workers", Count.workers())
                    .param("minerals", A.minerals())
                    .param("reserved", ReservedResources.minerals())
                    .param("supplyUsed", A.supplyUsed())
                    .param("supplyTotal", A.supplyTotal())
                    .log(A.now(), A.minSec());
            }
            return false;
        }

        ProduceWorker.produceWorker();
        return false;
    }

    // =========================================================

    public static boolean shouldProduceWorkers() {
        if (A.supplyFree() == 0 && A.supplyUsed() >= 11) return dont("NoSupply");
        if (!A.hasMinerals(50)) return dont("CantAfford");

        // Goal-board driver (NEXT #91): emergency preemption only. Engine
        // facts stay first, legacy strategy gates stay after.
        BoardState board = GoalBoardAdvisor.snapshot();
        if (GoalBoardDriver.pauseWorkers(board)) {
            return dont("BoardEmergency");
        }

        if (prioritizeRush()) return dont("RushPriority");

        int workers = Count.workers();
        if (workers >= 80) return dont("MaxedWorkers");

        // BO "At 8 Pylon" means BUILD it at 8 supply, not queue it eventually
        // (owner ruling 2026-10-13): with no Pylon at all, every worker built
        // below Pylon+worker money (150) pushes the Pylon further out -
        // measured as bank-400-while-blocked. Released by a started Pylon
        // (even warping) or by the 150 bank.
        if (
            We.protoss() && A.supplyUsed() >= 8
                && Count.ofTypeWithUnfinished(Protoss_Pylon) == 0
                && !A.hasMinerals(150)
        ) return dont("PylonAt8");

        // D3 priority waiting (generalizes SaveForNexus, which this
        // replaces: with a HIGH Nexus waiter the numbers are identical -
        // worker 50 + Nexus 400 = 450): a higher waiter pauses the pump
        // unless the bank covers worker + waiter. Only convertible (READY
        // or IN_PROGRESS) waiters count - a NOT_READY tech row must not
        // strangle the opening (owner 2026-10-14: continuous probe stream).
        if (PriorityWaiters.blocksWorkerSpending(50, 0) && !earlyConstantWorkerFlow()) {
            return dont("HigherPriorityWaits");
        }

        if (!A.hasMinerals(250) && !ProtossCriticalStuffInQueue.hasEnoughMinerals()) return dont("CriticalStuff");

        // Board absorption (NEXT #93 track): the worker Core-veto's posture
        // inputs arrive on the board snapshot now. The trio is gone with
        // EarlyGameProduceWorkers - TRUE and INDIFFERENT both proceeded, so
        // only the FALSE branch survives, verbatim, reason string included.
        if (We.protoss() && board.coreAwaitingSupply && A.minerals() <= 209) {
            return dont("PrioritizeCore");
        }

        if (
            A.supplyUsed() <= 40 && workers >= 25 && (
                (!A.hasMinerals(150) && A.minerals() < A.reservedMinerals() + 36))
        ) return dont("FollowBO");

        // === Protoss ===========================================

        if (We.protoss()) {
            if (
                Enemy.zerg() && workers >= 20 && !A.hasMinerals(175)
            ) return dont("MineralsMargin");

        }

        // === Terran ===========================================

        if (We.terran()) {
            if (
                A.hasGas(50) && SoonInQueue.have(AUnitType.Terran_Comsat_Station, 1)
            ) return dont("Wait4Comsat");
        }

        // =========================================================

        if (workers <= 20 && A.seconds() >= 600) return true;

        // === Zerg ===========================================

        if (We.zerg()) {
            if (A.supplyUsed() <= 20 && !A.canAffordWithReserved(50, 0)) return dont("EarlyReserved");
            ;
            if (Have.spawningPoolWithUnfinished() && Count.zerglingsWithUnfinished() <= 3)
                return A.hasMinerals(100) && Count.larvas(3);
            if (!A.hasMinerals(75 * Count.creepColonies())) return dont("CreepColonies");
            ;
            if (!A.hasMinerals(250) && SoonInQueue.have(Zerg_Spawning_Pool, 1)) {
                ProductionOrder order = Queue.get().nextOrders(1).ofType(Zerg_Spawning_Pool).get(0);
                if (order != null && order.supplyRequirementFulfilled(0)) {
                    return dont("Not250Min");
                }
            }

            if (A.supplyUsed() <= 15 && Count.zerglings() < 4) {
                int zerglingsInQueue = CountInQueue.count(AUnitType.Zerg_Zergling, 2);
                if (!A.hasMinerals(zerglingsInQueue * 50 + 50)) return dont("TooFewMin");
            }
        }

        // =========================================================

        // Check if not TOO MANY WORKERS
        int workersBonus = We.terran() ? 4 : 0; // For repairers
        if (workers >= (workersBonus + 25 * Select.ourBuildingsWithUnfinished().bases().count())) {
            return dont("TooMany");
        }

        // =========================================================
        // Check if AUTO-PRODUCTION of WORKERS is active.

        return isAutoProduceWorkersActive(workers) || dont("Limit");
    }

    /**
     * Owner 2026-10-16: automatic early worker flow. At SupplyTotal 10-20
     * a shortfall of at most 24 minerals does not stop the pump, even with
     * something else queued (measured: Gateway+Core waiters strangled
     * workers to a 400 bank while the Gateway itself never started). The
     * worker pays itself back; the 24 band only forgives the waiter gap,
     * never affordability itself (that gate stands above).
     */
    private static boolean earlyConstantWorkerFlow() {
        if (A.supplyTotal() < 10 || A.supplyTotal() > 20) return false;
        int shortfall = 50
            + PriorityWaiters.mineralsAboveReady(ProductionOrderPriority.STANDARD)
            - A.minerals();
        return shortfall <= 24;
    }

    private static boolean prioritizeRush() {        if (!Strategy.get().isRushOrCheese()) return false;

        return A.s <= 60 * 6 && !A.hasMinerals(92) && (
            (!We.terran() || Have.barracks())
        );
    }

    private static boolean dont(String reason) {
        REASON = reason;

        return false;
    }

    /**
     * Request to produce worker (Zerg Drone, Terran SCV or Protoss Probe) that should be handled according to
     * the race played.
     * <p>
     * See AutoProduceWorkersCommander which is also used to produce workers.
     */
    public static boolean produceWorker(AUnit base) {
        if (!A.canAfford(50, 0)) return false;
        if (A.supplyUsed() >= 8 && !hasEnoughMineralsToConsiderProducingWorker()) return false;

        if (We.zerg() && ProduceZergUnit.produceZergUnit(
            AtlantisRaceConfig.WORKER,
            ForcedDirectProductionOrder.create(AtlantisRaceConfig.WORKER)
        )) return true;

        if (base != null && base.train(
            AtlantisRaceConfig.WORKER,
            ForcedDirectProductionOrder.create(AtlantisRaceConfig.WORKER)
        )) return true;

//        if (CountInQueue.count(AtlantisRaceConfig.WORKER) > 0) return false;

        if (queueWorker()) return true;

        return false;
    }

    private static boolean queueWorker() {
        AUnit base = Select.main();
        if (base == null) return false;
        if (!base.isFree()) return false;

//        System.err.println("base.remainingTrainTime() = " + base.remainingTrainTime());

        // Owner 2026-10-14: queue the next worker when the current one has
        // 15 frames left (was 10), so the production never gaps. The engine
        // holds at most a few queued units; 15 of 300 frames (~5%) is queue
        // depth, not banked money.
        if (base.remainingTrainTime() <= NEXT_WORKER_WHEN_TRAINING_REMAINS) {
//                ProductionOrder order = AddToQueue.maxAtATime(AtlantisRaceConfig.WORKER, 1);
//            System.err.println("base.hasNothingInQueue() = " + base.hasNothingInQueue());
//            System.err.println("base.trainForced(AtlantisRaceConfig.WORKER) = " + base.trainForced(AtlantisRaceConfig.WORKER));
            if (base.hasNothingInQueue() && base.trainForced(AtlantisRaceConfig.WORKER)) {
//                System.err.println("Queueing worker at " + A.supplyUsed() + ", min:" + A.minerals());
                return true;
            }
        }

        return false;
    }

    private static boolean hasEnoughMineralsToConsiderProducingWorker() {
        if (ReservedResources.minerals() == 0) return true;

        return A.minerals() >= 200 || (ReservedResources.minerals() - A.minerals()) >= 50;
    }

    // =========================================================

    public static boolean isAutoProduceWorkersActive(int workers) {
        int autoProduceMinWorkers = BuildOrderSettings.autoProduceWorkersMinWorkers();
        if (A.supplyUsed() < autoProduceMinWorkers) return false;

        if (A.supplyUsed() <= 150) return true;

        int autoProduceMaxWorkers = BuildOrderSettings.autoProduceWorkersMaxWorkers();
        return workers < autoProduceMaxWorkers;
    }

}
