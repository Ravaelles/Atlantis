package tests.acceptance;

import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.util.Options;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitHelper;
import tests.unit.DynamicMockOurUnits;

import java.util.ArrayList;

import static atlantis.units.AUnitType.Terran_Barracks;
import static atlantis.units.AUnitType.Terran_Command_Center;
import static atlantis.units.AUnitType.Terran_SCV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The invariant behind _AI/BUGS.md B-9, named and tested.
 *
 * <p>The queue does not detect that a building is rising.
 * {@code IsOrderInProgress.isInProgress} has its unit branch commented out ("this
 * will happen in OnOurUnitCreated"), so on every {@code Queue.refresh()} an
 * unfinished building's order is decided by
 * {@code IsReadyToProduceOrder.isReady} - and the only thing in there that keeps
 * an order out of READY_TO_PRODUCE is:</p>
 *
 * <pre>
 *     if (construction != null &amp;&amp; construction.buildingUnit() != null &amp;&amp; buildingUnit().hp() &gt; 0)
 *         return false;
 * </pre>
 *
 * <p>So the queue's correctness rests on an invariant nobody states and nobody
 * checks: <b>every IN_PROGRESS building order has a construction whose building
 * unit is alive</b>. Nothing in the code verifies it, so a missed construction does
 * not fail loudly - the order quietly becomes ready again while the building is
 * still rising, which is duplicate production.</p>
 *
 * <p>This test does not fix that; it makes the coupling visible. The first half
 * shows the invariant doing its job, the second shows exactly what happens when it
 * does not hold - which is the case B-9 asks the owner to decide about.</p>
 */
public class QueueInProgressInvariantTest extends WorldStubForTests {
    private FakeUnit barracks = null;
    private ProductionOrder barracksOrder = null;

    @Test
    public void anInProgressBuildingOrderIsHeldThereByItsConstruction() {
        options = Options.create().set("supplyUsed", 66);
        world(2, ourInitialUnits(), fakeExampleEnemies(), () -> {
            if (A.now() == 1) frame1_barracksIsRising();
            if (A.now() == 2) frame2_theConstructionIsMissing();
        });
    }

    /**
     * A rising building, wired the way the game wires it: the unit knows its
     * order, the order and the unit share one construction, and the construction
     * knows the building unit. {@code OnOurUnitCreated} refreshes the queue, so
     * this frame exercises the real refresh, not a hand-set status.
     */
    private void frame1_barracksIsRising() {
        queue = initQueue();

        barracksOrder = queue.allOrders().ofType(Terran_Barracks).first();
        assertNotNull(barracksOrder, "the test build order asks for a barracks");

        barracks = fake(Terran_Barracks, 4).setCompleted(false);
        assertTrue(barracks.hp() > 0, "a rising building has hit points");

        Construction construction = new Construction(Terran_Barracks);
        construction.setBuildingUnit(barracks);
        barracks.setConstruction(construction);
        barracks.setProductionOrder(barracksOrder);
        barracksOrder.setConstruction(construction);

        ourUnitsNowInclude(barracks);
        atlantis.game.listeners.OnOurUnitCreated.update(barracks);

        assertEquals(OrderStatus.IN_PROGRESS, barracksOrder.status(),
            "an unfinished building whose construction is alive stays IN_PROGRESS "
                + "across a refresh - the queue cannot see the building itself, only "
                + "the construction");
        assertTrue(isHeldByALivingConstruction(barracksOrder),
            "the invariant B-9 names: an IN_PROGRESS building order has a construction "
                + "whose building unit is alive");
    }

    /**
     * The same world with one link missing - what happens when the construction
     * never got created, or the building unit was never wired to it. The order
     * does not fail, complain or stay put: it becomes ready to produce again,
     * while the building is still rising. That is consequence 1 of B-9, and it is
     * why the fix belongs in the queue (re-enable the unit branch) rather than in
     * a comment.
     */
    private void frame2_theConstructionIsMissing() {
        barracksOrder.setConstruction(null);
        barracks.setConstruction(null);

        atlantis.units.select.Select.clearCache();
        atlantis.production.orders.production.queue.Queue.get().refresh();

        assertEquals(OrderStatus.READY_TO_PRODUCE, barracksOrder.status(),
            "without the construction nothing tells the queue the building is rising, "
                + "so the order falls back to READY_TO_PRODUCE - the silent duplicate "
                + "production B-9 describes");
    }

    // =========================================================

    /**
     * The invariant, as a question. Returns whether the order is one the queue
     * cannot see the progress of - an IN_PROGRESS building order - and whether
     * something outside the queue is holding it there.
     */
    private boolean isHeldByALivingConstruction(ProductionOrder order) {
        if (!order.isStatus(OrderStatus.IN_PROGRESS)) return true;
        if (!order.isBuilding()) return true;

        Construction construction = order.construction();
        return construction != null
            && construction.buildingUnit() != null
            && construction.buildingUnit().hp() > 0;
    }

    private void ourUnitsNowInclude(FakeUnit... newFakeUnits) {
        ArrayList<FakeUnit> ourUnits = FakeUnitHelper.fakeUnitsToArrayList(ourInitialUnits());
        ourUnits.addAll(FakeUnitHelper.fakeUnitsToArrayList(newFakeUnits));

        DynamicMockOurUnits.mockOur(ourUnits);
        if (queue != null) queue.clearCache();
        if (queue != null) queue.refresh();
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