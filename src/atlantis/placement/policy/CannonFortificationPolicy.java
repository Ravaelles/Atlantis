package atlantis.placement.policy;

/**
 * How many Photon Cannons a base should have, re-derived from the legacy
 * `ShouldSecureProtossBase` heuristics (`_AI/redesign/03_PLACEMENT.md` §5.3,
 * §5.4
 * S5).
 *
 * <p>
 * The spec is emphatic about the boundary: <b>this is policy, not
 * placement.</b>
 * "This base should be fortified, with N cannons" is a statement about what we
 * want; where each cannon goes is the planner's business. So this class answers
 * only two questions - how many, and roughly where - and hands both to the goal
 * layer as a count and a {@code TargetPlacement} constraint. It never computes
 * a
 * tile.
 * </p>
 *
 * <p>
 * Pure: everything it reads is passed in, so it is unit-testable and the engine
 * adapter is the only place that touches the game. The numbers are the legacy
 * ones, kept so the rewrite does not silently change behaviour.
 * </p>
 */
public final class CannonFortificationPolicy {

    /** What the policy needs to know; the engine adapter fills it. */
    public static final class Context {
        public final int supplyTotal;
        public final int minerals;
        public final int existingCannonsAtBase;
        public final boolean enemyIsZerg;
        public final boolean enemyIsProtoss;
        public final int enemyMutalisks;

        public Context(
                int supplyTotal, int minerals, int existingCannonsAtBase,
                boolean enemyIsZerg, boolean enemyIsProtoss, int enemyMutalisks) {
            this.supplyTotal = supplyTotal;
            this.minerals = minerals;
            this.existingCannonsAtBase = existingCannonsAtBase;
            this.enemyIsZerg = enemyIsZerg;
            this.enemyIsProtoss = enemyIsProtoss;
            this.enemyMutalisks = enemyMutalisks;
        }
    }

    private CannonFortificationPolicy() {
    }

    /**
     * How many cannons this base should end up with. The legacy table, preserved:
     * a mineral-tier and a supply-tier bonus on top of one, plus zerg supply
     * milestones (mutas arrive at 130/180/190), with the Protoss cap and the
     * zerg early-game reduction the original applied.
     */
    public static int expectedCannons(Context context) {
        int total = 1;

        if (context.supplyTotal >= 40)
            total++; // second cannon
        if (context.minerals >= 540)
            total++;
        if (context.minerals >= 640)
            total++;
        if (context.minerals >= 800)
            total++;

        if (context.enemyIsZerg) {
            if (context.supplyTotal >= 130)
                total++;
            if (context.supplyTotal >= 180)
                total++;
            if (context.supplyTotal >= 190)
                total++;
        }

        if (context.enemyIsProtoss) {
            total = Math.min(total, 5 + (context.minerals >= 700 ? 1 : 0));
        }

        if (context.enemyIsZerg && context.supplyTotal <= 150 && context.minerals <= 550) {
            total = Math.min(3, total);
        }

        return Math.max(1, total + mutaBonus(context));
    }

    /**
     * Extra cannons for a mutalisk flock: one per four mutas, plus mineral tiers.
     */
    private static int mutaBonus(Context context) {
        if (context.enemyMutalisks <= 0)
            return 0;
        return context.enemyMutalisks / 4 + context.minerals / 500;
    }

    /** True when this base needs more cannons than it has. */
    public static boolean needsMoreCannons(Context context) {
        return context.existingCannonsAtBase < expectedCannons(context);
    }

    /** How many more to ask for right now. */
    public static int missingCannons(Context context) {
        return Math.max(0, expectedCannons(context) - context.existingCannonsAtBase);
    }
}
