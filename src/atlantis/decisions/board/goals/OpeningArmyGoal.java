package atlantis.decisions.board.goals;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;

/** Opening fighting force. */
public final class OpeningArmyGoal implements Goal {

    public static final int OPENING_ARMY_MIN = 6;

    @Override
    public String id() {
        return "army";
    }

    @Override
    public boolean active(BoardState state) {
        return state.combatUnits < OPENING_ARMY_MIN;
    }

    @Override
    public int rank() {
        return GoalRanks.ARMY;
    }
}
