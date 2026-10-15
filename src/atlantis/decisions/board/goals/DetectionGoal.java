package atlantis.decisions.board.goals;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;

/** Unseen enemy with no answer in place: preempts everything urgent. */
public final class DetectionGoal implements Goal {

    @Override
    public String id() {
        return "detection";
    }

    @Override
    public boolean active(BoardState state) {
        return state.enemyHasHiddenUnits && !state.hasDetection();
    }

    @Override
    public int rank() {
        return GoalRanks.DETECTION;
    }

    @Override
    public boolean emergency() {
        return true;
    }
}
