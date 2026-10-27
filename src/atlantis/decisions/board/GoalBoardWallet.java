package atlantis.decisions.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The wallet: serve the ranked ladder top-down, committing each goal's
 * price class from a virtual bank. The top goal and emergencies always
 * fund (a supply block or a cloaked wraith does not wait for a calm
 * bank); the rest fund only when the remainder covers them, in ladder
 * order. Skipped goals wait for the next frame instead of dying.
 *
 * <p>Pure and engine-free like the ranking itself: the bank arrives as
 * primitives from the production caller, prices arrive on the goals.
 * Feeding stays spend-free downstream (readiness and builders still
 * decide), so an unfunded goal loses a queue row for a frame, never a
 * building.</p>
 */
public final class GoalBoardWallet {

    private GoalBoardWallet() {
    }

    /**
     * Funded subset of the active goals, ladder order.
     */
    public static List<Goal> funded(BoardState state, Collection<Goal> goals, int minerals, int gas) {
        List<Goal> ranked = GoalBoard.ranked(state, goals);
        List<Goal> funded = new ArrayList<Goal>();
        int minLeft = minerals;
        int gasLeft = gas;
        boolean first = true;
        for (Goal goal : ranked) {
            if (first || goal.emergency()) {
                minLeft -= goal.priceMinerals();
                gasLeft -= goal.priceGas();
                funded.add(goal);
                first = false;
            }
            else if (goal.priceMinerals() <= minLeft && goal.priceGas() <= gasLeft) {
                minLeft -= goal.priceMinerals();
                gasLeft -= goal.priceGas();
                funded.add(goal);
            }
        }
        return funded;
    }
}
