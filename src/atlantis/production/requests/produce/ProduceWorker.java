package atlantis.production.requests.produce;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.config.AtlantisRaceConfig;
import atlantis.decisions.board.ADecision;
import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.DecisionDispatcher;
import atlantis.decisions.goals.protoss.WorkerQuestion;
import atlantis.production.GoalBoardAdvisor;
import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

import static atlantis.battle.siting.RebaseToNewMineralPatches.isBaseMinedOut;

public class ProduceWorker {
    // The door's domain half must be registered before the first issuance -
    // including in tests, which never run the game root (OnGameStarted).
    // Idempotent: re-registration only swaps an identical executor.
    static {
        atlantis.production.ProductionDecisionExecutor.register();
    }

    public static boolean produceWorker() {
        return produceWorker(null);
    }

    /**
     * Every worker train walks through the single door: the question
     * answers want, the call site names the act (BO line vs pump), the
     * dispatcher logs and executes. The queue mechanics underneath
     * (produceWorker on the commander) are untouched - Phase A.
     */
    public static boolean produceWorker(ProductionOrder order) {
        if (We.zerg()) {
            if (AGame.supplyUsed() >= 10 && Count.larvas() <= 1) return false;
        }

        AUnit base = baseToProduceWorker();
        if (base == null) return false;

        BoardState state = GoalBoardAdvisor.snapshot();
        ADecision want = new WorkerQuestion().ask(state);
        if (want == null) return false;

        return DecisionDispatcher.issue(want.refine(
            AtlantisRaceConfig.WORKER.name(),
            order == null ? "pump-flow" : order.toString(),
            order != null && !order.isDynamic()
                ? ADecision.Source.BUILD_ORDER : ADecision.Source.DYNAMIC,
            base).atFrame(A.now()));
    }

    private static AUnit baseToProduceWorker() {
        // Owner 2026-10-16: the old `.free()` filter excluded every training
        // base, so a busy Nexus never received a train order and the queue
        // path behind it was unreachable too. Prefer a free, empty base for
        // an immediate train; else fall back to a base that can take a
        // queued worker behind the running one (at most one waits).
        AUnit queueable = null;
        for (AUnit base : Select.ourBases().reverse().notLifted().list()) {
            if (!isSafeToProduceWorkerAt(base)) continue;
            if (isBaseMinedOut(base)) continue;
            if (base.isFree() && base.trainingQueue().isEmpty()) return base;
            if (queueable == null && AutoProduceWorkersCommander.canQueueWorkerAt(base)) {
                queueable = base;
            }
        }

        return queueable;
    }

    protected static boolean isSafeToProduceWorkerAt(AUnit base) {
        return base.enemiesNear().groundUnits().canAttack(base, 6).atMost(1);
    }
}
