package atlantis.decisions.board.goals;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;

/** Tech unlock for Dragoons, needs a Gateway first. */
public final class CyberneticsCoreGoal implements Goal {

    @Override
    public String id() {
        return "core";
    }

    @Override
    public boolean active(BoardState state) {
        return !state.hasCyberneticsCore && state.gatewayCount >= 1;
    }

    @Override
    public int rank() {
        return GoalRanks.CORE;
    }
}
