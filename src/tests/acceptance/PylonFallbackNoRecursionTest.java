package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.protoss.ProtossPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Race;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The Pylon relaxed-margin fallback must run exactly once.
 *
 * <p>Measured on OpenBW: {@code findStandardPositionFor} called
 * {@code findWithRelaxedBoundaryMargin} whenever a Pylon sweep failed, and the
 * relaxed pass called back into the same method with no re-entry guard - so a
 * search that failed twice recursed until {@code StackOverflowError}
 * (alternating frame 189 &lt;-&gt; 206), killing the bot instead of reporting
 * "no tile". A zero search radius fails on both passes by construction, which
 * is what pins the guard without needing a map.</p>
 */
public class PylonFallbackNoRecursionTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void failedPylonSearchReturnsNullInsteadOfOverflowing() {
        world(1, () -> {
            AUnit builder = Select.ourWorkers().first();
            assertNotNull(builder);

            APosition found = ProtossPositionFinder.findStandardPositionFor(
                builder, AUnitType.Protoss_Pylon, APosition.create(30, 30), 0
            );

            assertNull(found);
            assertFalse(
                ProtossPositionFinder.relaxedBoundaryMargin,
                "the fallback flag must not leak into the next search"
            );
        });
    }
}
