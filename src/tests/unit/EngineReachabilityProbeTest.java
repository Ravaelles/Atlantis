package tests.unit;

import atlantis.placement.engine.EngineReachabilityProbe;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Detecting that the engine's reachability cannot be trusted.
 *
 * <p>
 * The failure being detected is measured: on OpenBW the engine answers
 * {@code false} for
 * a tile paired with <b>itself</b>, because its query is region-based and the
 * server
 * publishes no regions ({@code _AI/CHALLENGES/OpenBW.md}). One query separates
 * a healthy
 * engine from a region-less one, and these tests pin both outcomes - a probe
 * that always
 * answered the same way would be worse than none, because the whole placement
 * path would
 * silently take the wrong branch.
 * </p>
 */
@Tag("placement")
public class EngineReachabilityProbeTest {

    private static final int TILE_X = 40;
    private static final int TILE_Y = 40;

    /** A healthy engine: regions exist, so a tile trivially reaches itself. */
    @Test
    public void aSaneEngineIsReportedUsable() {
        EngineReachabilityProbe.Verdict verdict = EngineReachabilityProbe.verdictFor(
                (tx, ty) -> true, TILE_X, TILE_Y);

        assertEquals(
                EngineReachabilityProbe.Verdict.ENGINE_USABLE, verdict,
                "an engine that answers 'a tile reaches itself' is usable");
    }

    /**
     * <b>The OpenBW case, measured.</b> No regions means no answer: the query
     * returns
     * false even for a position against itself, and the engine must be reported
     * broken so
     * the map-data implementation takes over.
     */
    @Test
    public void aRegionLessEngineIsReportedMissingRegions() {
        EngineReachabilityProbe.Verdict verdict = EngineReachabilityProbe.verdictFor(
                (tx, ty) -> false, TILE_X, TILE_Y);

        assertEquals(
                EngineReachabilityProbe.Verdict.ENGINE_REGIONS_MISSING, verdict,
                "false for a tile against itself is the region-less signature - the engine's "
                        + "answers must not be trusted, including the one inside canBuild");
    }

    /**
     * The probe is asked about the specific tile it was given. A probe that ignored
     * its
     * arguments and answered from a constant would pass both tests above, so this
     * one
     * makes the call observable.
     */
    @Test
    public void theProbeAsksAboutTheTileItWasGiven() {
        int[] asked = { -1, -1 };

        EngineReachabilityProbe.verdictFor(
                (tx, ty) -> {
                    asked[0] = tx;
                    asked[1] = ty;
                    return true;
                },
                TILE_X, TILE_Y);

        assertEquals(TILE_X, asked[0], "the probe must ask about the walkable tile it was given");
        assertEquals(TILE_Y, asked[1], "on both axes");
    }

    /** A missing query is treated as "cannot be trusted", not as "assume fine". */
    @Test
    public void aMissingQueryIsNotAssumedUsable() {
        assertEquals(
                EngineReachabilityProbe.Verdict.ENGINE_REGIONS_MISSING,
                EngineReachabilityProbe.verdictFor(null, TILE_X, TILE_Y),
                "an absent answer must fail safe: assuming usable is what puts the broken "
                        + "region query back in charge");
    }
}
