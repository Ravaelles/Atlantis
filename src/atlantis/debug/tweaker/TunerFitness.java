package atlantis.debug.tweaker;

/**
 * Scalar fitness for tuner batches: hits taken times game minutes,
 * lower is better. A duel that ends fast with few hits on us beats a
 * long bloody one, whatever the absolute scales are.
 *
 * <p>A loss carries a flat +1M penalty on top, so one number orders
 * every row: wins compete on speed and blood, losses compete on dying
 * slower and cleaner, and no loss ever outranks a win (1M dwarfs any
 * realistic score - hundreds of hits times tens of minutes). The
 * penalty is the reusable half for other scenarios: it keeps a single
 * comparable scalar instead of a two-tier gate, so gradient slopes stay
 * meaningful across mixed outcomes (a constant shifts nothing - least
 * squares centers it away - while the ordering still prefers winning).
 * Unknown counters (the {@code ?} rows) read as infinite:
 * a fully-instrumented game always beats a blind one.
 *
 * <p>Pure tooling, no game-loop callers.
 */
public final class TunerFitness {

    /** Flat loss penalty: dwarfs any realistic hits x minutes score. */
    public static final double LOSS_PENALTY = 1000000.0;

    private TunerFitness() {
    }

    /** Game seconds as minutes. Non-positive or unknown reads as infinite (unusable row). */
    public static double minutes(int seconds) {
        if (seconds <= 0 || seconds == Integer.MAX_VALUE) return Double.MAX_VALUE;

        return seconds / 60.0;
    }

    /**
     * TIMES_HIT x GAME_length_IN_MINUTES. Unknown hits or an unusable
     * clock read as infinite. Zero hits on a finished clock is a
     * perfect zero, never a guard clause.
     */
    public static double fitness(int hitsTaken, int seconds) {
        if (hitsTaken < 0 || hitsTaken == Integer.MAX_VALUE) return Double.MAX_VALUE;

        double minutes = minutes(seconds);
        if (minutes == Double.MAX_VALUE) return Double.MAX_VALUE;

        return hitsTaken * minutes;
    }

    /**
     * Scalar fitness of a batch row: TIMES_HIT x GAME_minutes, plus the
     * flat loss penalty for a LOSS. Unknown rows stay infinite, never
     * arithmetic on a sentinel.
     */
    public static double fitness(TunerObjective.Row row) {
        if (row == null) return Double.MAX_VALUE;

        double base = fitness(row.hitsTaken(), row.seconds());
        if (base == Double.MAX_VALUE) return Double.MAX_VALUE;

        return row.win() ? base : base + LOSS_PENALTY;
    }

    /**
     * Negative when {@code a} is the better game: lower fitness wins,
     * then the game name breaks ties so rankings are stable across
     * runs. The loss penalty inside the scalar already orders every
     * loss below every win, so no separate outcome branch is needed.
     */
    public static int compare(TunerObjective.Row a, TunerObjective.Row b) {
        int byFitness = Double.compare(fitness(a), fitness(b));
        if (byFitness != 0) return byFitness;

        return a.game().compareTo(b.game());
    }
}
