package tests.acceptance;

import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Options;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitHelper;
import tests.unit.DynamicMockOurUnits;

import java.util.ArrayList;
import java.util.Collection;

import static atlantis.units.AUnitType.Terran_Barracks;
import static atlantis.units.AUnitType.Terran_Command_Center;
import static atlantis.units.AUnitType.Terran_SCV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * B-9: a rising building must consume its own order, whether or not the
 * construction object survived - and stop consuming it the moment it stops rising.
 *
 * <p>Before the fix, {@code IsOrderInProgress.isInProgress} had its unit branch
 * commented out ("for units this will happen in OnOurUnitCreated"), and
 * {@code OnOurUnitCreated} only ever <i>set</i> IN_PROGRESS - nothing checked it
 * afterwards. So on every {@code Queue.refresh()} an unfinished building's order was
 * decided by {@code IsReadyToProduceOrder.isReady}, and the only thing in there that
 * kept it out of READY_TO_PRODUCE was:</p>
 *
 * <pre>
 *     if (construction != null &amp;&amp; construction.buildingUnit() != null &amp;&amp; buildingUnit().hp() &gt; 0)
 *         return false;
 * </pre>
 *
 * <p>The queue's correctness therefore rested on an invariant nobody stated and
 * nobody checked: <b>every IN_PROGRESS building order has a construction whose
 * building unit is alive</b>. Nothing verified it, so a missed construction did not
 * fail loudly - the order quietly became ready again while the building was still
 * rising, which is duplicate production.</p>
 *
 * <p>Progress is now derived from the units themselves
 * ({@code IsOrderInProgress.forUnit}: we have N units of this type and M other
 * orders of the same type are finished or in progress, so this order is in progress
 * when N &gt; M). That is a question about units rather than about construction
 * objects, so the two tests below pin the whole lifecycle:</p>
 *
 * <ul>
 *   <li>{@link #anInProgressBuildingOrderIsHeldThereByItsConstruction} - a barracks
 *       at 3 hit points with most of its build time left stays IN_PROGRESS with both
 *       construction links intact <i>and</i> with both removed (the frame that used
 *       to fall back to READY_TO_PRODUCE), and it becomes FINISHED when the engine
 *       says the building is done;</li>
 *   <li>{@link #aDestroyedBuildingMakesItsOrderReadyAgain} - a rising building that
 *       is destroyed goes back to READY, so progress detection cannot stall
 *       production either.</li>
 * </ul>
 */
public class QueueInProgressInvariantTest extends WorldStubForTests {
    private FakeUnit barracks = null;
    private ProductionOrder barracksOrder = null;

    @Test
    public void anInProgressBuildingOrderIsHeldThereByItsConstruction() {
        options = Options.create().set("supplyUsed", 66);
        world(3, ourInitialUnits(), fakeExampleEnemies(), () -> {
            if (A.now() == 1) frame1_barracksIsRising();
            if (A.now() == 2) frame2_theConstructionIsMissing();
            if (A.now() == 3) frame3_theBuildingIsCompleted();
        });
    }

    @Test
    public void aDestroyedBuildingMakesItsOrderReadyAgain() {
        options = Options.create().set("supplyUsed", 66);
        world(2, ourInitialUnits(), fakeExampleEnemies(), () -> {
            if (A.now() == 1) frame1_barracksIsRising();
            if (A.now() == 2) frame2_theBuildingIsDestroyed();
        });
    }

    /**
     * A rising building, wired the way the game wires it: the unit knows its order,
     * the order and the unit share one construction, and the construction knows the
     * building unit. {@code OnOurUnitCreated} refreshes the queue, so this frame
     * exercises the real refresh, not a hand-set status.
     *
     * <p>The barracks is at 3 hit points, not at full health: the queue must not care
     * how far along the building is, only that it exists and is unfinished.</p>
     */
    private void frame1_barracksIsRising() {
        queue = initQueue();

        barracksOrder = queue.allOrders().ofType(Terran_Barracks).first();
        assertNotNull(barracksOrder, "the test build order asks for a barracks");

        barracks = fake(Terran_Barracks, 4).setCompleted(false).setHp(3);

        Construction construction = new Construction(Terran_Barracks);
        construction.setBuildingUnit(barracks);
        barracks.setConstruction(construction);
        barracks.setProductionOrder(barracksOrder);
        barracksOrder.setConstruction(construction);

        ourUnitsNowInclude(barracks);
        atlantis.game.listeners.OnOurUnitCreated.update(barracks);
        // Count caches its answers for a few frames and this frame ran the bot's
        // logic before the barracks existed, so drop it - the order status was
        // already decided by the listener above.
        Count.clearCache();

        assertEquals(3, barracks.hp(), "the building has barely started");
        assertEquals(1, Select.countOurOfTypeWithUnfinished(Terran_Barracks),
            "and this is the very question the queue asks: one unfinished barracks, "
                + "even though it is at 3 hit points");
        assertEquals(OrderStatus.IN_PROGRESS, barracksOrder.status(),
            "an unfinished building stays IN_PROGRESS across a refresh");
    }

    /**
     * The same world with both construction links removed - what used to happen when
     * the construction never got created, or the building unit was never wired to it:
     * the order became ready to produce again while the building was still rising.
     *
     * <p>It does not any more. Progress is read off the units, so the answer is the
     * same as in frame 1 and the construction links are irrelevant - which is the
     * whole point, since nothing in the game guarantees they exist.</p>
     */
    private void frame2_theConstructionIsMissing() {
        barracksOrder.setConstruction(null);
        barracks.setConstruction(null);

        refreshTheQueue();

        assertEquals(3, barracks.hp(), "the building is still at 3 hit points");
        assertEquals(OrderStatus.IN_PROGRESS, barracksOrder.status(),
            "the queue sees the building itself, so it does not fall back to "
                + "READY_TO_PRODUCE and produce a second barracks");
    }

    /**
     * The other end of the same question: a building that is finished must stop
     * counting, and completion is still an engine event
     * ({@code OnOurNewUnitCompleted}), not something the queue infers. Before the
     * fix this transition was the only thing that ever ended IN_PROGRESS, and it is
     * still - what changed is that progress no longer depends on it to *start*.
     */
    private void frame3_theBuildingIsCompleted() {
        barracks.setCompleted(true);
        atlantis.game.listeners.OnOurNewUnitCompleted.ourNewUnitCompleted(barracks);

        assertEquals(OrderStatus.FINISHED, barracksOrder.status(),
            "a completed building finishes its order");
        assertEquals(0, CountInQueue.countInProgress(Terran_Barracks),
            "and the queue owes us no more barracks");
        assertEquals(1, Select.countOurOfTypeWithUnfinished(Terran_Barracks),
            "a completed unit still counts as a unit of this type - it is the "
                + "event, not the count, that ends an order");
    }

    /**
     * Progress must not be sticky. A destroyed building leaves the unit list in the
     * real game and {@code OnOurUnitDestroyed} refreshes the queue - that is the
     * whole of the signal, so this frame simulates exactly that.
     */
    private void frame2_theBuildingIsDestroyed() {
        // Destruction in the game: hit points go to 0 and the unit leaves the list
        // we query. Both, because the queue asks two different questions - "does a
        // unit of this type exist" (the list) and "is the building unit alive"
        // (its hit points, via IsReadyToProduceOrder's construction check).
        barracks.setHp(0);
        ourUnitsNowExclude(barracks);
        refreshTheQueue();

        assertEquals(OrderStatus.READY_TO_PRODUCE, barracksOrder.status(),
            "the building is gone, so the queue owes us a barracks again");
    }

    // =========================================================

    private void refreshTheQueue() {
        Count.clearCache();
        Select.clearCache();
        atlantis.production.orders.production.queue.Queue.get().refresh();
    }

    private void ourUnitsNowInclude(FakeUnit... newFakeUnits) {
        mockOur(FakeUnitHelper.fakeUnitsToArrayList(
            FakeUnitHelper.merge(ourInitialUnits(), newFakeUnits)));
    }

    private void ourUnitsNowExclude(FakeUnit unit) {
        ArrayList<FakeUnit> ourUnits = FakeUnitHelper.fakeUnitsToArrayList(ourInitialUnits());
        ourUnits.remove(unit);
        mockOur(ourUnits);
    }

    private void mockOur(Collection<FakeUnit> ourUnits) {
        DynamicMockOurUnits.mockOur(ourUnits);
        if (queue != null) queue.clearCache();
    }

    private FakeUnit[] ourInitialUnits() {
        return fakeOurs(
            fake(Terran_Command_Center, 10),
            fake(Terran_SCV, 11),
            fake(Terran_SCV, 12),
            fake(Terran_SCV, 13),
            fake(Terran_SCV, 14)
        );
    }
}