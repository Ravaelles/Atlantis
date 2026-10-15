package atlantis.decisions.board;

/**
 * One thing the bot may want. A goal never touches builders, positions or
 * the engine: it answers whether it applies to a state and where it stands
 * on the fixed ladder. Affordability is NOT a goal's business - the wallet
 * (execution walking the ranking top-down) skips what the bank cannot
 * cover, and the goal waits for the next frame instead of dying.
 */
public interface Goal {

    /** Stable identity, also used for the in-flight commitment set. */
    String id();

    /** True when this goal wants work in the given state. */
    boolean active(BoardState state);

    /**
     * Fixed ladder position, lower runs first. Constants live in the domain
     * ladder ({@code goals.GoalRanks}) - tuning strategy is domain knowledge,
     * the sort itself is generic machinery that never names a game concept.
     */
    int rank();

    /**
     * Emergency goals break the in-flight commitment - a cloaked wraith
     * does not wait for a calm plan. Which goals are emergencies is domain
     * knowledge (only DetectionGoal says true); the break mechanics is
     * generic.
     */
    default boolean emergency() {
        return false;
    }
}
