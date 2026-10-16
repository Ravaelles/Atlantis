package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.ADecision;
import atlantis.decisions.board.AQuestion;
import atlantis.decisions.board.BoardState;

/**
 * Should a worker train now? Lenient outer envelope only (Phase A):
 * supply free, bank covers the price, headroom under the cap. The pump's
 * inner gates (reservations, waiters, mandates) still decide inside the
 * executor - this question adds the audit row and the single door, and
 * sharpens into the real gate in Phase B.
 */
public final class WorkerQuestion implements AQuestion {

    @Override
    public ADecision ask(BoardState state) {
        if (state == null) return null;
        if (state.supplyFree() <= 0) return null;
        if (state.minerals < 50) return null;
        if (state.workerCount >= 25 * Math.max(1, state.baseCount)) return null;

        return ADecision.decide(
            ADecision.Kind.TRAIN_WORKER, "Probe", "pump-flow", ADecision.Source.DYNAMIC)
            .frame(0).supply(state.supplyUsed, state.supplyTotal)
            .bank(state.minerals, state.gas)
            .build();
    }
}
