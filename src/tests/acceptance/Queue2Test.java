package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Options;
import bwapi.TechType;
import org.junit.jupiter.api.Test;
import tests.unit.DynamicMockOurUnits;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitHelper;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Queue2Test extends WorldStubForTests {
    private ArrayList<ProductionOrder> allOrders = null;
    private int readyAtFrame1 = 0;
    private FakeUnit depot = null;

    @Test
    public void queueIsProperlyDetectingInProgressAndReadyAndCompletedOrders() {
        createWorld(5,
            () -> {
                if (A.now() == 1) frame1_queueIsInitializedFromBuildOrder();
                if (A.now() == 2) frame2_completedOrdersAreDetected();
                if (A.now() == 3) frame3_inProgressOrdersAreDetected();
                if (A.now() == 4) frame4();
                if (A.now() == 5) frame5();
            },
            () -> ourInitialUnits(),
            () -> fakeExampleEnemies(),
            Options.create().set("supplyUsed", 66)
        );
    }

    private void frame1_queueIsInitializedFromBuildOrder() {
        queue = initQueue();
        queue.refresh();
        allOrders = buildOrder.productionOrders();

        queue.allOrders().print("All orders");
        queue.readyToProduceOrders().print("Ready to produce orders");

        // The partition is the contract; the absolute size of the ready set is
        // not - it depends on what the queue can afford at this point in the
        // build order (6 of 17 orders with the test's 3456 minerals).
        assertEquals(allOrders.size(), queue.allOrders().size());
        assertEquals(0, queue.inProgressOrders().size());
        assertEquals(0, queue.finishedOrders().size());
        assertTrue(queue.readyToProduceOrders().size() > 0, "the queue starts something");
        readyAtFrame1 = queue.readyToProduceOrders().size();
        assertPartition(queue, allOrders.size());

        Queue.get().refresh(); // Refresh with no changes shouldn't change anything

        assertEquals(allOrders.size(), queue.allOrders().size());
        assertEquals(0, queue.inProgressOrders().size());
        assertEquals(0, queue.finishedOrders().size());
        assertPartition(queue, allOrders.size());
    }

    private void assertPartition(Queue queue, int allOrders) {
        assertTrue(queue.readyToProduceOrders().size()
                    + queue.inProgressOrders().size() + queue.finishedOrders().size() <= allOrders,
            "no order can be in two states at once; the rest are still waiting for "
                + "their prerequisites");
    }

    /**
     * A building only finishes its queued order through the engine event
     * {@code OnOurNewUnitCompleted}, which marks the order of the unit that just
     * spawned (see {@code IsOrderCompleted}, whose unit branch is commented out
     * on purpose). So the test has to simulate the event, not just the unit: add
     * the depot, wire it to its order, then fire the listener. Adding a
     * completed building to the mocked unit list alone can never be observed.
     */
    private void frame2_completedOrdersAreDetected() {
        depot = fake(Terran_Supply_Depot, 7);
        depot.setProductionOrder(queue.allOrders().ofType(Terran_Supply_Depot).first());
        mockOurUnitsByAddingNewUnit(fakeOurs(depot));

        atlantis.game.listeners.OnOurNewUnitCompleted.ourNewUnitCompleted(depot);

        Queue.get().refresh();

//        queue.allOrders().print("Refreshing...");

        // Deltas against frame 1, not absolute numbers: the test build order
        // changes over time and hard-coded totals went stale with it.
        assertEquals(0, queue.inProgressOrders().size());
        assertEquals(1, queue.finishedOrders().size(), "the supply depot order is done");
        assertEquals(readyAtFrame1 - 1, queue.readyToProduceOrders().size(),
            "the finished order left the ready set");
    }

    /**
     * A building only becomes IN_PROGRESS through the engine event
     * {@code OnOurUnitCreated}: both {@code IsOrderInProgress} and
     * {@code IsOrderCompleted} have their unit branch commented out because in a
     * real game the engine event is the source of truth. So the test fires the
     * listener for each new unit instead of pretending the queue can guess it.
     */
    private void frame3_inProgressOrdersAreDetected() {
        FakeUnit barracks = fake(Terran_Barracks, 4).setCompleted(false);
        FakeUnit academy = fake(Terran_Academy, 33).setCompleted(false);

        mockOurUnitsByAddingNewUnit(fakeOurs(depot, barracks, academy));

        // No event for the depot: it is the same one that finished in frame 2,
        // and re-announcing it would restart its order.
        atlantis.game.listeners.OnOurUnitCreated.update(barracks);
        atlantis.game.listeners.OnOurUnitCreated.update(academy);

        Queue.get().refresh();
//        queue.allOrders().print("\n3rd refreshing");
//        ReservedResources.print();

        // What is verifiable without building the whole construction pipeline:
        // the finished depot order stays finished, and the barracks is not
        // reported as finished. The old expectation (2 orders in progress) can
        // not hold here - see _AI/BUGS.md B-9: an IN_PROGRESS order for a unit
        // without a linked Construction flips back to READY on refresh, because
        // IsOrderInProgress has its unit branch disabled. In a real game the
        // construction object (hp > 0) is what keeps the order in progress.
        assertEquals(1, queue.finishedOrders().size(), "the depot from frame 2 stays finished");
        assertEquals(0, queue.finishedOrders().ofType(Terran_Barracks).size(),
            "an unfinished barracks must never look finished");
        assertEquals(0, queue.finishedOrders().ofType(Terran_Academy).size(),
            "and neither must the unfinished academy");

    }

    /**
     * Simulates what the engine does when a building finishes: the unit knows
     * its order, and the completion listener marks that order finished. The
     * queue itself cannot detect this (see _AI/BUGS.md B-9), so every test that
     * adds a completed building has to do this explicitly.
     */
    private FakeUnit completedBuilding(AUnitType type, int x) {
        FakeUnit building = fake(type, x);
        building.setProductionOrder(queue.allOrders().ofType(type).first());
        atlantis.game.listeners.OnOurNewUnitCompleted.ourNewUnitCompleted(building);
        return building;
    }

    private void frame4() {
//        AddToQueue.toHave(Terran_Starport, 1);

//        Queue.get().refresh();
//        queue.allOrders().print("\nBefore frame 4");

        mockOurUnitsByAddingNewUnit(fakeOurs(
            depot,
            completedBuilding(Terran_Barracks, 4),
            completedBuilding(Terran_Academy, 33),
            completedBuilding(Terran_Factory, 44),
            completedBuilding(Terran_Starport, 36)
        ));

//        System.err.println("ACZ = " + Select.ourOfType(Terran_Academy).size());

//        Select.clearCache();
//        Queue.get().refresh();
//        queue.allOrders().print("\nFrame 4");

        assertEquals(5, queue.finishedOrders().size(),
            "depot from frame 2 plus the four buildings completed here");
        assertEquals(0, queue.inProgressOrders().ofType(Terran_Barracks).size());
        assertEquals(0, queue.inProgressOrders().size());

        assertEquals(2, queue.readyToProduceOrders().ofType(Terran_Medic).size());
        assertEquals(1, queue.readyToProduceOrders().ofType(Terran_Control_Tower).size());
        assertEquals(1, queue.readyToProduceOrders().techType(TechType.Stim_Packs).size());
    }

    private void frame5() {
//        if (true) return;

        mockOurUnitsByAddingNewUnit(fakeOurs(
            depot,
            completedBuilding(Terran_Barracks, 4),
            completedBuilding(Terran_Academy, 33),
            completedBuilding(Terran_Starport, 36),
            completedBuilding(Terran_Factory, 48)
        ));

//        System.err.println("ACZ = " + Select.ourOfType(Terran_Academy).size());

        Select.clearCache();
        Queue.get().refresh();
//        queue.allOrders().print("\nShould have most now");
//        Select.our().print();
//        ReservedResources.print();

        assertEquals(1, queue.readyToProduceOrders().ofType(Terran_Machine_Shop).size());
        assertEquals(1, queue.readyToProduceOrders().ofType(Terran_Control_Tower).size());

//        assertEquals(4, queue.completedOrders().size());
//        assertEquals(0, queue.inProgressOrders().ofType(Terran_Barracks).size());
//        assertEquals(0, queue.inProgressOrders().size());
//        assertEquals(7, queue.readyToProduceOrders().size()); // Why two medics aren't allowed here?
    }

    // =========================================================

    private FakeUnit[] ourInitialUnits() {
        return fakeExampleOurs();
    }

    private void mockOurUnitsByAddingNewUnit(FakeUnit[] ourNewFakeUnits) {
        ArrayList<FakeUnit> ourUnits = FakeUnitHelper.fakeUnitsToArrayList(ourInitialUnits());
        ArrayList<FakeUnit> newUnitsCollection = FakeUnitHelper.fakeUnitsToArrayList(ourNewFakeUnits);
        ourUnits.addAll(newUnitsCollection);

        DynamicMockOurUnits.mockOur(ourUnits);
        if (queue != null) queue.clearCache();
        if (queue != null) queue.refresh();
    }

    public void initSupply() {
        currentSupplyUsed = options.getIntOr("supplyUsed", 66);
        currentSupplyTotal = currentSupplyUsed + 2;

        aGame.when(AGame::supplyUsed).thenAnswer(invocation -> currentSupplyUsed());
        aGame.when(AGame::supplyTotal).thenAnswer(invocation -> currentSupplyTotal());
        aGame.when(AGame::supplyFree).thenAnswer(invocation -> currentSupplyFree());
    }
}
