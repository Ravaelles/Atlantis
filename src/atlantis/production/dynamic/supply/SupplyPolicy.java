package atlantis.production.dynamic.supply;

/**
 * The single-door Pylon concurrency cap (SRP slice of the 2026-10-19
 * SOLID review, S3): at most one unfinished Pylon below 40 supply,
 * the pipeline pair at 40+; a rich bank lifts the early cap (the
 * Pylon + Core pair stays affordable together, so the cap gates
 * concurrency, never the demand).
 *
 * <p>Pure over plain ints - both legacy doors read this one rule
 * instead of re-spelling the bands. The in-flight counting stays
 * with {@code ConstructionRequests}, the demand bands with
 * {@code NeedDynamicSupply}, the V2 emission guard and the sim-exit
 * read with their own (different decisions, own thresholds).</p>
 */
public final class SupplyPolicy {

    /** Below this used supply only one Pylon flies at a time. */
    public static final int EARLY_SUPPLY_CAP = 40;
    /** A bank at or above this lifts the early cap (owner rule). */
    public static final int RICH_BANK_MINERALS = 300;
    public static final int EARLY_MAX_IN_FLIGHT = 1;
    public static final int LATE_MAX_IN_FLIGHT = 2;

    private SupplyPolicy() {
    }

    public static boolean tooManyPylonsInFlight(int inFlight, int supplyUsed, boolean richBank) {
        if (supplyUsed < EARLY_SUPPLY_CAP) return inFlight >= EARLY_MAX_IN_FLIGHT && !richBank;

        return inFlight >= LATE_MAX_IN_FLIGHT;
    }
}
