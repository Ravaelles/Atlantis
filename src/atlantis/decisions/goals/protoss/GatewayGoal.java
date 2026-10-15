package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;

/** Production capacity for the opening. */
public final class GatewayGoal implements Goal {

    public static final int WANTED_GATEWAYS = 2;

    @Override
    public String id() {
        return "gateway";
    }

    @Override
    public boolean active(BoardState state) {
        return state.gatewayCount < WANTED_GATEWAYS;
    }

    @Override
    public int rank() {
        return GoalRanks.GATEWAY;
    }
}
