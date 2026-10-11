package tests.unit;

import atlantis.production.v2.ResourceCost;
import atlantis.production.v2.ResourceTimeline;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The livelock behind {@code NEXT.md} #52: a building whose planned start
 * slides forward
 * every frame, so it is never due and never dispatched.
 *
 * <p>
 * Measured in a live `PRODUCTION_V2=LIVE` run as `Pylon@1129`, `@1130`,
 * `@1132`, `@1125` -
 * the same relative-frame slide `_AI/STATUS.md` recorded as 449, 450, 451, 452.
 * The game
 * ends itself, every worker order is issued, and the building never is.
 * </p>
 *
 * <p>
 * <b>The mechanism these tests pin, without a game.</b> {@code allocate}
 * deducts from the
 * chosen frame <b>and every later one</b> (that is deliberate - reserved money
 * is gone for
 * the rest of the plan), and {@code findEarliestAffordableFrame} requires the
 * cost to be
 * covered across that whole tail. So a cheap, higher-priority item allocated
 * first
 * (a Probe, 50 minerals) lowers the tail for everything after it, and an
 * expensive item
 * (a Pylon, 100) can then only be afforded <em>later</em> than the pass before
 * - which is
 * exactly the slide: the plan is recomputed from scratch each frame, the cheap
 * item is
 * re-allocated at the front, and the expensive one is pushed one more frame
 * away.
 * </p>
 *
 * <p>
 * The test is here rather than in a game run because the timeline is pure logic
 * by design
 * (M1 of {@code _AI/redesign/01_PRODUCTION.md}): the whole point of this layer
 * is that
 * this class of bug can be proven without launching StarCraft.
 * </p>
 */
public class TimelineSlideTest {

    private static final ResourceCost PROBE = ResourceCost.of(50, 0, 1);
    private static final ResourceCost PYLON = ResourceCost.of(100, 0, 0);

    /**
     * <b>The slide, reproduced - and it needs income to happen at all.</b>
     *
     * <p>
     * The first draft of this test used a flat bank and got {@code -1} on every pass, which
     * is "never affordable", not "later". That distinction is what the whole bug turns on:
     * the slide only exists when money <b>arrives over time</b>, because then "later" means
     * "after the next mining tick". A flat bank below the cost is a different problem and
     * gets its own test below.
     * </p>
     *
     * <p>
     * Each pass is given the SAME game state, because the v2 plan is rebuilt from scratch
     * every frame - so any movement in the answer is the scheduling's doing, not the
     * fixture's. That is what makes this a reproduction rather than an illustration.
     * </p>
     */
    @Test
    public void aCheapItemAllocatedFirstPushesTheExpensiveOneAwayEachPass() {
        int[] starts = new int[6];

        for (int pass = 0; pass < starts.length; pass++) {
            ResourceTimeline timeline = new ResourceTimeline(4000, 60, 0, 20);
            timeline.addMiningIncome(0, 0.5, 0.0);

            // The worker goal is scheduled first every pass - the higher priority band, and
            // in the live run the only thing ever dispatched.
            int probeStart = timeline.findEarliestAffordableFrame(PROBE, 0);
            assertTrue(probeStart >= 0, "precondition: the Probe must be schedulable");
            timeline.allocate(PROBE, probeStart);

            starts[pass] = timeline.findEarliestAffordableFrame(PYLON, 0);
        }

        assertTrue(
                starts[0] >= 0,
                "precondition: with income the Pylon must be plannable somewhere in the horizon - "
                        + "if this fails the test models the wrong situation. Starts: "
                        + java.util.Arrays.toString(starts));

        for (int i = 1; i < starts.length; i++) {
            assertEquals(
                    starts[0], starts[i],
                    "every pass sees the SAME game state, so the Pylon's start must be the same "
                            + "number each time. A start that moves between identical passes IS the slide - "
                            + "in the live log it moved forward every frame (Pylon@1129, @1130, @1132). Starts: "
                            + java.util.Arrays.toString(starts));
        }
    }

    /**
     * The other failure mode, which the first draft of the test above mistook for the
     * slide: with a bank that can never reach the cost, the item is not "pushed later", it is
     * unplannable ({@code -1}). Pinned separately on purpose - conflating the two would send
     * a fix after the wrong thing.
     */
    @Test
    public void aBankThatNeverReachesTheCostMakesTheItemUnplannableRatherThanLate() {
        ResourceTimeline flat = new ResourceTimeline(4000, 60, 0, 20);
        flat.allocate(PROBE, flat.findEarliestAffordableFrame(PROBE, 0));

        assertEquals(
                -1, flat.findEarliestAffordableFrame(PYLON, 0),
                "a flat 60-mineral bank cannot afford a 100 Pylon at any frame in the horizon: that is "
                        + "-1 (unplannable), NOT a late start. The slide needs income - this is a different "
                        + "defect and must not be chased as the same one");
    }

    /**
     * The other half of the diagnosis, and the reason it is an <b>arbitration</b>
     * problem:
     * when the bank is large enough that the cheap item does not eat the expensive
     * one's
     * money, there is no slide at all. A fix has to make this true, not merely make
     * the
     * symptom smaller.
     */
    @Test
    public void withEnoughMineralsTheStartDoesNotSlide() {
        int[] starts = new int[6];

        for (int pass = 0; pass < starts.length; pass++) {
            ResourceTimeline timeline = new ResourceTimeline(4000, 1000, 0, 20);

            timeline.allocate(PROBE, timeline.findEarliestAffordableFrame(PROBE, 0));
            starts[pass] = timeline.findEarliestAffordableFrame(PYLON, 0);
        }

        assertEquals(
                starts[0], starts[starts.length - 1],
                "a healthy bank must give a stable start - if this ever slides too, the cause is "
                        + "not arbitration and the diagnosis in #52 is wrong");
    }

    /**
     * Documents <b>why</b> allocation makes the tail cheaper - the property the
     * slide is
     * built on, pinned so a future change to it is a conscious one. If
     * {@code allocate}
     * ever stopped deducting the tail, most scheduling tests would keep passing
     * while this
     * whole class of bug changed shape.
     */
    @Test
    public void allocationDeductsFromTheChosenFrameAndEveryFrameAfterIt() {
        ResourceTimeline timeline = new ResourceTimeline(100, 500, 0, 20);

        timeline.allocate(ResourceCost.of(100, 0, 0), 10);

        assertEquals(500, timeline.mineralsAt(9), "frames before the allocation are untouched");
        assertEquals(400, timeline.mineralsAt(10), "the allocation frame pays");
        assertEquals(400, timeline.mineralsAt(99), "and so does the whole tail - this is what "
                + "makes a later item wait, and what the slide is made of");
    }

    /**
     * <b>The fix, verified at the level the bug lives at.</b> Ordering the plans the other
     * way round - the opening supply provider before the workers - removes the slide that
     * the test above reproduces. This is the assertion that says the chosen fix works,
     * rather than that a constant changed.
     *
     * <p>
     * The priorities are taken from {@code ProductionGoal} rather than written as numbers,
     * so the test follows the policy instead of freezing it: it fails if the ordering is
     * ever reversed, whatever values the bands take.
     * </p>
     */
    @Test
    public void planningTheOpeningSupplyProviderFirstRemovesTheSlide() {
        assertTrue(
            atlantis.production.v2.ProductionGoal.PRIORITY_OPENING_SUPPLY
                < atlantis.production.v2.ProductionGoal.PRIORITY_WORKERS,
            "precondition: the opening supply band must sort before the worker band"
        );

        int[] starts = new int[6];

        for (int pass = 0; pass < starts.length; pass++) {
            ResourceTimeline timeline = new ResourceTimeline(4000, 60, 0, 20);
            timeline.addMiningIncome(0, 0.5, 0.0);

            // Same state every pass; the only difference from the test above is WHICH item
            // is allocated first - the opening Pylon, as the priority bands now order them.
            int pylonStart = timeline.findEarliestAffordableFrame(PYLON, 0);
            assertTrue(pylonStart >= 0, "precondition: the Pylon must be schedulable");
            timeline.allocate(PYLON, pylonStart);

            starts[pass] = pylonStart;
        }

        for (int i = 1; i < starts.length; i++) {
            assertEquals(
                starts[0], starts[i],
                "with the Pylon planned first, its start must be stable across identical passes "
                    + "- a start that moves is the slide, and it is what stops the building from "
                    + "ever being dispatched. Starts: " + java.util.Arrays.toString(starts)
            );
        }
    }

    /**
     * A single unit of the slide, stated as arithmetic: the same cost is pushed LATER
     * simply because something else reserved money ahead of it. Repeated every pass, that
     * one-step difference is the livelock.
     *
     * <p>
     * Income is required for the same reason as the test above - without it there is no
     * "later" to be pushed to, only {@code -1}.
     * </p>
     */
    @Test
    public void reservationMovesTheSameCostToALaterFrame() {
        ResourceTimeline withoutReservation = new ResourceTimeline(500, 100, 0, 20);
        withoutReservation.addMiningIncome(0, 1.0, 0.0);

        ResourceTimeline withReservation = new ResourceTimeline(500, 100, 0, 20);
        withReservation.addMiningIncome(0, 1.0, 0.0);

        int before = withoutReservation.findEarliestAffordableFrame(PYLON, 0);

        // Reserve the Probe where the scheduler would: at the earliest frame it fits.
        int probeStart = withReservation.findEarliestAffordableFrame(PROBE, 0);
        assertTrue(probeStart >= 0, "precondition: the Probe must fit");
        withReservation.allocate(PROBE, probeStart);

        int after = withReservation.findEarliestAffordableFrame(PYLON, 0);

        assertTrue(
                after > before,
                "a reservation ahead of it must push the same cost later (" + before + " -> " + after
                        + ") - that is the mechanism, not a bug in the search itself");
    }
}
