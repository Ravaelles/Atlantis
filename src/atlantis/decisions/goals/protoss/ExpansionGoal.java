package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;

/**
 * Second base once the economy can carry it and nothing presses.
 * Affordability is the wallet's job, not this predicate's.
 */
public final class ExpansionGoal implements Goal {

    public static final int EXPANSION_MIN_WORKERS = 12;

    @Override
    public String id() {
        return "expansion";
    }

    @Override
    public boolean active(BoardState state) {
        return state.baseCount < 2
            && state.workerCount >= EXPANSION_MIN_WORKERS
            && !state.baseUnderAttack;
    }

    @Override
    public int rank() {
        return GoalRanks.EXPANSION;
    }
}
