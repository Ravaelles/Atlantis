package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.ADecision;
import atlantis.decisions.board.AQuestion;
import atlantis.decisions.board.BoardState;

/**
 * Should a building start now? Lenient outer envelope only (Phase A):
 * bank covers the cheapest building, minus one builder walk of headroom
 * (owner: dispatch early so the walk overlaps earning, not after).
 * Keep in sync with the IsReady headroom (same 50). Price, power,
 * placement and priority gates still decide inside the executor.
 */
public final class BuildingQuestion implements AQuestion {

    @Override
    public ADecision ask(BoardState state) {
        if (state == null) return null;
        if (state.minerals < 50) return null;

        return ADecision.decide(
                ADecision.Kind.BUILD, "building", "build-want", ADecision.Source.DYNAMIC)
            .frame(0).supply(state.supplyUsed, state.supplyTotal)
            .bank(state.minerals, state.gas)
            .build();
    }
}
