package tests.unit;

import atlantis.production.v2.ResourceCost;
import atlantis.production.v2.ResourceTimeline;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the timeline math of the production-v2 core (M1 of
 * _AI/redesign/01_PRODUCTION.md). Pure logic: no game, no stubs - the whole
 * point of this layer is that it can be proven without launching StarCraft.
 *
 * <p>
 * The invariants here are the ones the redesign calls architectural:
 * solvency (never negative), forward shifting instead of dropping, and the
 * "reservation is visible to later frames" semantics of allocate().
 * </p>
 */
public class ResourceTimelineTest {

    private static final ResourceCost DRAGOON = ResourceCost.of(125, 50, 2);

    @Test
    public void stocksStayFlatWithoutIncome() {
        ResourceTimeline timeline = new ResourceTimeline(100, 400, 100, 10);

        assertEquals(400, timeline.mineralsAt(0));
        assertEquals(400, timeline.mineralsAt(99));
        assertEquals(100, timeline.gasAt(50));
        assertEquals(10, timeline.supplyAvailableAt(99));
    }

    @Test
    public void miningIncomeAccumulatesLinearly() {
        ResourceTimeline timeline = new ResourceTimeline(100, 0, 0, 10);

        // 1 mineral and 0.5 gas per frame.
        timeline.addMiningIncome(0, 1.0, 0.5);

        assertEquals(1, timeline.mineralsAt(0));
        assertEquals(50, timeline.mineralsAt(49));
        assertEquals(100, timeline.mineralsAt(99));
        assertEquals(25, timeline.gasAt(49));
        // Gas accumulates as fractional per frame: at frame 99 it floor'd 50.5 -> 50.
        assertTrue(timeline.gasAt(99) >= 49, "gas at 99: " + timeline.gasAt(99));
    }

    @Test
    public void incomeStartingLaterLeavesEarlyFramesUntouched() {
        ResourceTimeline timeline = new ResourceTimeline(100, 0, 0, 10);

        timeline.addMiningIncome(50, 2.0, 0);

        assertEquals(0, timeline.mineralsAt(49));
        assertEquals(2, timeline.mineralsAt(50));
        assertEquals(100, timeline.mineralsAt(99));
    }

    @Test
    public void earliestAffordableFindsExactFrame() {
        ResourceTimeline timeline = new ResourceTimeline(200, 0, 0, 4);
        timeline.addMiningIncome(0, 1.0, 1.0);

        // A Dragoon (125m/50g) is affordable when the LAST of its three
        // thresholds is crossed. Frame F holds the income of F+1 frames
        // (frames 0..F inclusive), so 125 minerals are there at frame 124.
        assertEquals(124, timeline.findEarliestAffordableFrame(DRAGOON, 0));
    }

    @Test
    public void unaffordableWithinHorizonReturnsMinusOne() {
        ResourceTimeline timeline = new ResourceTimeline(10, 0, 0, 2);
        timeline.addMiningIncome(0, 1.0, 0);

        assertEquals(-1, timeline.findEarliestAffordableFrame(DRAGOON, 0));
    }

    @Test
    public void supplyShortageBlocksAffordability() {
        ResourceTimeline timeline = new ResourceTimeline(200, 500, 200, 2);

        // Plenty of minerals and gas, but a Dragoon needs 2 supply and only 2 is
        // free... with 2 available it IS affordable; drop to 1 and it never is.
        ResourceTimeline shortSupply = new ResourceTimeline(200, 500, 200, 1);
        assertEquals(-1, shortSupply.findEarliestAffordableFrame(DRAGOON, 0));
        assertEquals(0, timeline.findEarliestAffordableFrame(DRAGOON, 0));
    }

    @Test
    public void allocateSubtractsFromThatFrameOnwards() {
        ResourceTimeline timeline = new ResourceTimeline(100, 400, 0, 10);

        timeline.allocate(ResourceCost.of(100, 0, 2), 40);

        assertEquals(400, timeline.mineralsAt(39));
        assertEquals(300, timeline.mineralsAt(40));
        assertEquals(300, timeline.mineralsAt(99));
        assertEquals(8, timeline.supplyAvailableAt(40));
        assertEquals(10, timeline.supplyAvailableAt(39));
    }

    @Test
    public void allocateMakesLaterItemsWaitBehindEarlierOnes() {
        // The core "priority by sequence" semantics: with no initial stock and
        // 1 mineral per frame, three 100-mineral zealots must schedule at
        // frames 99, 199 and 299 - each one waiting for the income the
        // previous reservation did not consume. This is the "shift forward,
        // never drop" behaviour the redesign is about.
        ResourceTimeline timeline = new ResourceTimeline(400, 0, 0, 10);
        timeline.addMiningIncome(0, 1.0, 0);

        ResourceCost zealot = ResourceCost.of(100, 0, 2);

        int first = timeline.findEarliestAffordableFrame(zealot, 0);
        timeline.allocate(zealot, first);

        int second = timeline.findEarliestAffordableFrame(zealot, first);
        timeline.allocate(zealot, second);

        int third = timeline.findEarliestAffordableFrame(zealot, second);
        timeline.allocate(zealot, third);

        assertTrue(second > first, "second must be after first: " + first + " vs " + second);
        assertTrue(third > second, "third must be after second: " + second + " vs " + third);
        assertEquals(99, first);
        assertEquals(199, second);
        assertEquals(299, third);
    }

    @Test
    public void negativeStartStocksAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ResourceTimeline(100, -1, 0, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new ResourceTimeline(0, 100, 0, 10));
    }

    @Test
    public void overspendingIsRejectedBySolvencyCheck() {
        ResourceTimeline timeline = new ResourceTimeline(100, 400, 0, 10);

        // 500 minerals is never available; allocating must throw rather than
        // silently produce a plan the economy cannot pay for.
        assertThrows(IllegalStateException.class,
                () -> timeline.allocate(ResourceCost.of(500, 0, 0), 0));
    }

    @Test
    public void canAffordAtMatchesFindEarliest() {
        ResourceTimeline timeline = new ResourceTimeline(200, 0, 0, 4);
        timeline.addMiningIncome(0, 1.0, 1.0);

        int found = timeline.findEarliestAffordableFrame(DRAGOON, 0);
        assertTrue(timeline.canAffordAt(DRAGOON, found));
        assertFalse(timeline.canAffordAt(DRAGOON, found - 1));
    }
}
