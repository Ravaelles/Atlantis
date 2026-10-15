package atlantis.decisions.board.goals;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.decisions.board.GoalRanks;

/** Gas income, Core-first. */
public final class AssimilatorGoal implements Goal {

    @Override
    public String id() {
        return "assimilator";
    }

    @Override
    public boolean active(BoardState state) {
        return !state.hasAssimilator && state.hasCyberneticsCore;
    }

    @Override
    public int rank() {
        return GoalRanks.ASSIMILATOR;
    }
}
