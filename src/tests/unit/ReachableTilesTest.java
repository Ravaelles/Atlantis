package tests.unit;

import atlantis.map.ReachableTiles;
import atlantis.placement.core.Reachability;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The one place the game asks "can a ground unit get from A to B".
 *
 * <p>
 * This port exists because that question used to be asked with a raw
 * {@code Atlantis.game().hasPath(...)} from several callers, and on OpenBW the
 * engine
 * answers {@code false} for every pair of tiles - even a tile against itself -
 * because its
 * query is region-based and the server publishes no regions (measured
 * 2026-10-10,
 * {@code _AI/CHALLENGES/OpenBW.md}). One caller had a fallback, another was
 * switched to
 * it, the rest kept asking the broken question.
 * </p>
 *
 * <p>
 * <b>What these tests cover:</b> the delegation contract and the seam. The
 * resolution
 * itself is measured in {@code tests.unit.EngineReachabilityProbeTest} (sane
 * engine vs
 * region-less one); here the concern is that every caller reaches the SAME
 * implementation
 * and that a null or absent answer cannot become a "yes" by accident.
 * </p>
 */
public class ReachableTilesTest {

    @BeforeEach
    @AfterEach
    public void clearChosenImplementation() {
        ReachableTiles.reset();
    }

    /**
     * A caller gets the implementation the port was told to use - the seam works.
     */
    @Test
    public void callsGoThroughTheInstalledImplementation() {
        int[] calls = { 0 };
        ReachableTiles.useReachability(
                (fromTx, fromTy, toTx, toTy) -> {
                    calls[0]++;
                    return true;
                },
                "test double");

        assertTrue(ReachableTiles.between(10, 10, 20, 20), "the installed answer is returned");
        assertEquals(1, calls[0], "exactly one delegation, not a re-resolved implementation");
    }

    /**
     * The tile-based and position-based entry points must agree: two shapes of the
     * same
     * question that answered differently is how the six raw callers drifted apart
     * in the
     * first place.
     */
    @Test
    public void thePositionAndTileEntryPointsAgree() {
        ReachableTiles.useReachability((fromTx, fromTy, toTx, toTy) -> true, "always reachable");

        assertTrue(ReachableTiles.between(3, 4, 5, 6), "tile form");

        atlantis.map.position.APosition from = atlantis.map.position.APosition.create(3, 4);
        atlantis.map.position.APosition to = atlantis.map.position.APosition.create(5, 6);
        assertTrue(ReachableTiles.between(from, to), "position form must not disagree");
    }

    /**
     * A null argument is "not reachable", never an accidental pass. Placement code
     * hands
     * these methods positions that can legitimately be null (an absent nearTo, a
     * building
     * with no tile yet), and a NullPointerException there would kill the frame.
     */
    @Test
    public void nullPositionsAreNotReachable() {
        ReachableTiles.useReachability((fromTx, fromTy, toTx, toTy) -> true, "always reachable");

        assertFalse(ReachableTiles.between(null, null), "both null");
        assertFalse(
                ReachableTiles.between(atlantis.map.position.APosition.create(1, 1), null),
                "a null target is not reachable, even when the implementation would say yes");
    }

    /**
     * The reason is exposed so a run can say which implementation was in charge.
     */
    @Test
    public void theChosenReasonIsReported() {
        ReachableTiles.useReachability((fromTx, fromTy, toTx, toTy) -> true, "because the test said so");

        assertEquals("because the test said so", ReachableTiles.chosenReason());
    }

    /**
     * The reflexivity rule from {@code Reachability}'s contract, asserted at the
     * port as
     * well as in the implementation: a tile reaching itself is the property the
     * engine
     * violates, and the port is where a caller relying on it should be able to say
     * so.
     */
    @Test
    public void aReflexiveImplementationIsWhatCallersMayAssume() {
        Reachability reflexive = (fromTx, fromTy, toTx, toTy) -> fromTx == toTx && fromTy == toTy;

        ReachableTiles.useReachability(reflexive, "reflexive only");

        assertTrue(ReachableTiles.between(7, 7, 7, 7), "a tile reaches itself");
        assertFalse(ReachableTiles.between(7, 7, 7, 8), "and this implementation says no further");
    }
}
