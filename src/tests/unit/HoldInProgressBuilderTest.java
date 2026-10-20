package tests.unit;

import atlantis.production.constructions.builders.BuilderManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Protoss builder stays assigned while its IN_PROGRESS construction has no
 * building warping - releasing it feeds the Builder/Gather alternation.
 *
 * <p>
 * Measured on Wine (builder diagnostics): every assigned builder alternating
 * {@code BuilderManager} / {@code GatherResources} frame by frame while
 * standing next to its site, nothing ever built. The status had flipped on a
 * refused attempt's order echo ({@code isConstructing} true for one frame),
 * and the old rule released every stopped worker - so Gather stole the
 * builder on each still frame and the build never resumed. Only a building
 * actually warping at the site releases the worker now.
 * </p>
 */
@Tag("production")
public class HoldInProgressBuilderTest {

    @Test
    public void movingBuilderStaysAssigned() {
        assertTrue(
            BuilderManager.holdInProgressBuilder(false, false),
            "a worker walking to or around the site stays assigned either way");
        assertTrue(
            BuilderManager.holdInProgressBuilder(false, true),
            "a moving worker with a warping building stays assigned either way");
    }

    @Test
    public void stoppedBuilderWithoutWarpingStaysAssigned() {
        assertTrue(
            BuilderManager.holdInProgressBuilder(true, false),
            "stopped with nothing warping is a refused echo, not a warp - "
                + "releasing it lets Gather steal it every still frame");
    }

    @Test
    public void stoppedBuilderWithWarpingIsReleased() {
        assertFalse(
            BuilderManager.holdInProgressBuilder(true, true),
            "stopped next to a warping building is the healthy shape - "
                + "the warp continues without the worker");
    }
}
