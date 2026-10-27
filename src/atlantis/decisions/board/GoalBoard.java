package atlantis.decisions.board;

import java.util.*;

/**
 * The priority board: pure ranking over goal vocabulary. No builders, no
 * positions, no engine reads - ranking a stub state takes microseconds,
 * which is the whole point (arbitration stops costing OpenBW games).
 *
 * <p>Execution serves the ranking top-down against a virtual wallet and
 * skips what the bank cannot cover; skipped goals wait for the next frame,
 * never die. That wallet walk lives with the consumers, not here.</p>
 */
public final class GoalBoard {

    private GoalBoard() {
    }

    /**
     * Active goals, ladder order.
     */
    public static List<Goal> ranked(BoardState state, Collection<Goal> goals) {
        List<Goal> active = new ArrayList<Goal>();
        for (Goal goal : goals) {
            if (goal.active(state)) {
                active.add(goal);
            }
        }
        Collections.sort(active, new Comparator<Goal>() {
            @Override
            public int compare(Goal first, Goal second) {
                return Integer.compare(first.rank(), second.rank());
            }
        });

        return active;
    }

    /**
     * The goal to serve first. An in-flight goal (started, unfinished)
     * keeps its place - the anti-flap commitment our cancel history paid
     * for - unless an emergency goal is active, which always wins.
     * Null when nothing is active.
     */
    public static Goal top(BoardState state, Collection<Goal> goals, Set<String> inFlight) {
        List<Goal> ranked = ranked(state, goals);
        if (ranked.isEmpty()) {
            return null;
        }
        for (Goal goal : ranked) {
            if (goal.emergency()) {
                return goal;
            }
        }
        if (inFlight != null) {
            for (Goal goal : ranked) {
                if (inFlight.contains(goal.id())) {
                    return goal;
                }
            }
        }
        return ranked.get(0);
    }
}
