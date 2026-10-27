package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.ADecision;
import atlantis.decisions.board.AQuestion;
import atlantis.decisions.board.BoardState;

/**
 * Should an army unit train now? Lenient outer envelope only (Phase A):
 * supply free, a gateway standing, bank covers a zealot. Dragoon gas,
 * tech and composition gates still decide inside the executor.
 */
public final class ArmyQuestion implements AQuestion {

    @Override
    public ADecision ask(BoardState state) {
        if (state == null) return null;
        if (state.supplyFree() <= 0) return null;
        if (state.gatewayCount <= 0) return null;
        if (state.minerals < 100) return null;

        return ADecision.decide(
                ADecision.Kind.TRAIN_ARMY, "army", "army-flow", ADecision.Source.DYNAMIC)
            .frame(0).supply(state.supplyUsed, state.supplyTotal)
            .bank(state.minerals, state.gas)
            .build();
    }
}
