package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Select;
import atlantis.util.Options;
import bwapi.TechType;
import org.junit.Test;
import tests.unit.DynamicMockOurUnits;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitHelper;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;
import static org.junit.Assert.assertEquals;

public class Queue2Test extends NonAbstractTestFakingGame {
    private ArrayList<ProductionOrder> allOrders = null;

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

        assertEquals(allOrders.size(), queue.allOrders().size());
        assertEquals(0, queue.inProgressOrders().size());
        assertEquals(6, queue.readyToProduceOrders().size());
        assertEquals(0, queue.completedOrders().size());

        Queue.get().refresh(); // Refresh with no changes shouldn't change anything

        assertEquals(allOrders.size(), queue.allOrders().size());
        assertEquals(0, queue.inProgressOrders().size());
        assertEquals(6, queue.readyToProduceOrders().size());
        assertEquals(0, queue.completedOrders().size());
    }

    private void frame2_completedOrdersAreDetected() {
        mockOurUnitsByAddingNewUnit(fakeOurs(
            fake(Terran_Supply_Depot, 7)
        ));

        Queue.get().refresh();

//        queue.allOrders().print("Refreshing...");

        assertEquals(0, queue.inProgressOrders().size());
        assertEquals(5, queue.readyToProduceOrders().size());
        assertEquals(1, queue.completedOrders().size());
    }

    private void frame3_inProgressOrdersAreDetected() {
        mockOurUnitsByAddingNewUnit(fakeOurs(
            fake(Terran_Supply_Depot, 7),
            fake(Terran_Barracks, 4).setCompleted(false),
            fake(Terran_Academy, 33).setCompleted(false)
        ));

        Queue.get().refresh();
//        queue.allOrders().print("\n3rd refreshing");
//        ReservedResources.print();

        assertEquals(1, queue.inProgressOrders().ofType(Terran_Barracks).size());
        assertEquals(2, queue.inProgressOrders().size());
        assertEquals(4, queue.readyToProduceOrders().size());
        assertEquals(1, queue.completedOrders().size());
    }

    private void frame4() {
//        AddToQueue.toHave(Terran_Starport, 1);

//        Queue.get().refresh();
//        queue.allOrders().print("\nBefore frame 4");

        mockOurUnitsByAddingNewUnit(fakeOurs(
            fake(Terran_Supply_Depot, 7),
            fake(Terran_Barracks, 4),
            fake(Terran_Academy, 33),
            fake(Terran_Factory, 44),
            fake(Terran_Starport, 36)
        ));

//        System.err.println("ACZ = " + Select.ourOfType(Terran_Academy).size());

//        Select.clearCache();
//        Queue.get().refresh();
//        queue.allOrders().print("\nFrame 4");

        assertEquals(5, queue.completedOrders().size());
        assertEquals(0, queue.inProgressOrders().ofType(Terran_Barracks).size());
        assertEquals(0, queue.inProgressOrders().size());

        assertEquals(2, queue.readyToProduceOrders().ofType(Terran_Medic).size());
        assertEquals(1, queue.readyToProduceOrders().ofType(Terran_Control_Tower).size());
        assertEquals(1, queue.readyToProduceOrders().techType(TechType.Stim_Packs).size());
    }

    private void frame5() {
//        if (true) return;

        mockOurUnitsByAddingNewUnit(fakeOurs(
            fake(Terran_Supply_Depot, 7),
            fake(Terran_Barracks, 4),
            fake(Terran_Academy, 33),
            fake(Terran_Starport, 36),
            fake(Terran_Factory, 48)
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
        int supplyFree = 2;
        int supplyUsed = options.getIntOr("supplyUsed", 66);

        aGame.when(AGame::supplyUsed).thenReturn(supplyUsed);
        aGame.when(AGame::supplyFree).thenReturn(supplyFree);
        aGame.when(AGame::supplyTotal).thenReturn(supplyUsed + supplyFree);
    }
}
