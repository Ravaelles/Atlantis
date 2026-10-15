package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;

/** Blocked supply: nothing trains without it. */
public final class SupplyPylonGoal implements Goal {

    @Override
    public String id() {
        return "supply-pylon";
    }

    @Override
    public boolean active(BoardState state) {
        return state.baseCount > 0 && state.supplyFree() <= 2;
    }

    @Override
    public int rank() {
        return GoalRanks.SUPPLY;
    }
}
