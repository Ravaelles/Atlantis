package atlantis.decisions.board;

/**
 * Fixed ladder positions, lower runs first. One diff retunes the strategy:
 * no threshold is buried in a producer anymore. Initial values mirror the
 * live doctrine (supply block first, emergency preemption, economy before
 * tech, expansion last); games move them, tests pin them.
 */
public final class GoalRanks {

    private GoalRanks() {
    }

    /** Supply blocked: nothing trains without it. */
    public static final int SUPPLY = 10;

    /**
     * Emergency ceiling: any active goal at or above this rank breaks the
     * in-flight commitment (a cloaked wraith does not wait for a calm plan).
     */
    public static final int EMERGENCY_BREAK = 20;

    /** Unseen enemy with no answer in place. */
    public static final int DETECTION = 20;

    /** Continuous economy. */
    public static final int WORKERS = 30;

    /** Production capacity. */
    public static final int GATEWAY = 40;

    /** Tech unlock for Dragoons. */
    public static final int CORE = 50;

    /** Gas income, Core-first. */
    public static final int ASSIMILATOR = 60;

    /** Opening fighting force. */
    public static final int ARMY = 70;

    /** Second base, last in the opening. */
    public static final int EXPANSION = 80;
}
