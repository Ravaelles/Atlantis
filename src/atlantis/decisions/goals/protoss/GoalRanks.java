package atlantis.decisions.goals.protoss;

/**
 * Domain ladder (ubiquitous language): strategy tuning in one diff, owned
 * by the domain package. The generic machinery sorts ints and never reads
 * this file's meaning - that direction is the whole point (CONVENTIONS
 * §21). Initial values mirror the live doctrine; games move them, tests
 * pin them.
 */
public final class GoalRanks {

    /**
     * Supply blocked: nothing trains without it.
     */
    public static final int SUPPLY = 10;
    /**
     * Unseen enemy with no answer in place (also an emergency).
     */
    public static final int DETECTION = 20;
    /**
     * Continuous economy.
     */
    public static final int WORKERS = 30;
    /**
     * Production capacity.
     */
    public static final int GATEWAY = 40;
    /**
     * Tech unlock for Dragoons.
     */
    public static final int CORE = 50;
    /**
     * Gas income, Core-first.
     */
    public static final int ASSIMILATOR = 60;
    /**
     * Forge timing: DT prediction plus matchup supply table.
     */
    public static final int FORGE = 61;
    /**
     * Wall-in at the natural: post-core rush-window defense.
     */
    public static final int WALL_IN = 65;
    /**
     * Static defense count (teeth for the wall, answer to air).
     */
    public static final int CANNON = 66;
    /**
     * Opening fighting force.
     */
    public static final int ARMY = 70;
    /**
     * Sustained combat-unit flow past the opening (both postures share the seat).
     */
    public static final int SUSTAINED_ARMY = 75;
    /**
     * Second base, last in the opening.
     */
    public static final int EXPANSION = 80;

    private GoalRanks() {
    }
}
