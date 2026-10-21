package atlantis.debug.tweaker;

/**
 * Scalar fitness for tuner batches: hits taken times game minutes,
 * lower is better. A duel that ends fast with few hits on us beats a
 * long bloody one, whatever the absolute scales are.
 *
 * <p>The win gate is deliberate, not an afterthought: without it a fast
 * loss with few hits would outrank a hard-fought win, which is exactly
 * backwards for a combat tuner. Wins always rank above losses; the
 * scalar only orders within the same outcome. Unknown counters (the
 * {@code ?} rows) read as infinitely bad - a fully-instrumented game
 * always beats a blind one.
 *
 * <p>Pure tooling, no game-loop callers.
 */
public final class TunerFitness {

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

    /** Scalar fitness of a batch row. */
    public static double fitness(TunerObjective.Row row) {
        if (row == null) return Double.MAX_VALUE;
        return fitness(row.hitsTaken(), row.seconds());
    }

    /**
     * Negative when {@code a} is the better game: a WIN always beats a
     * LOSS, then lower fitness wins, then the game name breaks ties so
     * rankings are stable across runs.
     */
    public static int compare(TunerObjective.Row a, TunerObjective.Row b) {
        if (a.win() != b.win()) return a.win() ? -1 : 1;
        int byFitness = Double.compare(fitness(a), fitness(b));
        if (byFitness != 0) return byFitness;
        return a.game().compareTo(b.game());
    }
}
