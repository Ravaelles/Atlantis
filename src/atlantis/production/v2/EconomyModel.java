package atlantis.production.v2;

/**
 * The constant-rate economy approximation used by the timeline (Stardust's
 * {@code MINERALS_PER_WORKER_FRAME} model; BWAPI exposes no forward gathering
 * simulation). Approximations, not measurements: kept in one place so they are
 * tunable and testable.
 */
public final class EconomyModel {

    /** ~8 minerals per trip, one trip per ~180 frames on a saturated line. */
    public static final double MINERALS_PER_WORKER_FRAME = 0.045;

    /** ~8 gas per trip on a 3-worker geyser. */
    public static final double GAS_PER_WORKER_FRAME = 0.07;

    /**
     * Frames a fresh worker needs to walk to the line and return its first load.
     */
    public static final int NEW_WORKER_FIRST_TRIP_FRAMES = 120;

    /** Beyond this many mineral workers per base, extra workers add nothing. */
    public static final int SATURATED_MINERAL_WORKERS_PER_BASE = 18;

    private EconomyModel() {
    }

    /**
     * Income rate of {@code workers} mining on {@code bases}, ignoring
     * over-saturation.
     */
    public static double mineralRate(int workers, int bases) {
        int effective = Math.min(Math.max(0, workers), SATURATED_MINERAL_WORKERS_PER_BASE * Math.max(1, bases));
        return effective * MINERALS_PER_WORKER_FRAME;
    }
}
