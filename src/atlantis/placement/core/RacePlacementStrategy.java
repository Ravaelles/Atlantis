package atlantis.placement.core;

/**
 * The single extension point for racial differences in placement
 * (`_AI/redesign/03_PLACEMENT.md` §4.1-§4.3).
 *
 * <p>
 * The core does the race-neutral work: build the tile grid, stamp blocks, emit
 * a
 * ranked candidate list, hand out a reservation. What differs between races is
 * <b>four questions</b>, and they are all here - so adding Terran later is this
 * interface plus a strategy, with zero edits to the core (OCP on the race
 * axis).
 * </p>
 *
 * <p>
 * The contract deliberately carries no Protoss concept: nothing here mentions
 * Psi,
 * Pylons or cannon chokes. "Powered" is expressed as
 * {@link #framesUntilAvailable}, whose <i>meaning</i> the strategy owns
 * (Protoss:
 * Pylon power; Terran: addon availability; Zerg: creep), while the core only
 * compares the number.
 * </p>
 */
public interface RacePlacementStrategy {

    /** Generic result of checking whether a candidate can be used. */
    enum AvailabilityVerdict {
        AVAILABLE,
        NEEDS_SUPPORT,
        REFUSE
    }

    /**
     * The block templates this race uses, in the order they should be tried.
     * Protoss ships 24 normal blocks and four start variants (S2); Terran and Zerg
     * return their own sets when implemented.
     */
    java.util.List<BuildBlock.Spec> blockTemplates();

    /**
     * When a tile of this race's building becomes usable, in frames from now:
     * {@code 0} now, a positive frame when something must complete first,
     * {@code -1} never. Protoss answers from Pylon power; Terran from addon
     * availability and lift/land; Zerg from projected creep.
     */
    int framesUntilAvailable(int tx, int ty, String buildableTypeId);

    /**
     * Does a building of this type need the tile to be "available" before it can
     * start? Protoss: only power-needing buildings. Terran: buildings that need an
     * addon. Zerg: anything off creep.
     */
    boolean requiresAvailability(String buildableTypeId);

    /**
     * Evaluates a candidate that requires race-specific availability. The default
     * implementation uses the projected availability frame; races that can request
     * missing support may return {@link AvailabilityVerdict#NEEDS_SUPPORT}.
     */
    default AvailabilityVerdict availabilityVerdict(int tx, int ty, String buildableTypeId) {
        return framesUntilAvailable(tx, ty, buildableTypeId) >= 0
            ? AvailabilityVerdict.AVAILABLE
            : AvailabilityVerdict.REFUSE;
    }

    /**
     * Requests this race's support for a candidate whose verdict was
     * {@link AvailabilityVerdict#NEEDS_SUPPORT}: Protoss asks for a Pylon, a race
     * that can never create support simply does nothing. The planner never needs to
     * know which building that is.
     */
    default void requestAvailabilitySupport(int tx, int ty, String buildableTypeId) {
        // Default: a race with no support channel has nothing to request.
    }

    /**
     * Optional extra ordering weight for a candidate. Returning 0 means "no
     * opinion" and leaves the generic ranker's order alone - which is what a race
     * with no special preference answers everywhere.
     */
    default int extraRankingWeight(BuildLocation location, String buildableTypeId) {
        return 0;
    }
}
