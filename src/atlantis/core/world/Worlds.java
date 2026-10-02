package atlantis.core.world;

/**
 * Stage E (see _AI/REVIEW.md §16): transitional holder of the shared
 * production registry.
 *
 * <p>Unit identity still needs one process-wide home until the frame pipeline
 * builds a fresh {@link World} per frame. New code should prefer explicit
 * {@link UnitRegistry} instances (tests already do); this holder dies when
 * the pipeline owns worlds directly.</p>
 */
public final class Worlds {

    private static UnitRegistry units = new UnitRegistry();

    private Worlds() {
    }

    public static UnitRegistry units() {
        return units;
    }

    /**
     * Tests (and engine restarts) start from an empty registry.
     */
    public static void reset() {
        units = new UnitRegistry();
    }
}
