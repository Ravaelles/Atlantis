package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.TerranStrategies;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.QueueInitializer;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.util.Options;
import bwapi.TechType;
import org.junit.Test;
import tests.unit.DynamicMockOurUnits;
import tests.unit.FakeUnit;
import tests.unit.FakeUnitHelper;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;
import static org.junit.Assert.assertEquals;

public class Queue2Test extends NonAbstractTestFakingGame {
    private ABuildOrder buildOrder;
    private Queue queue = null;
    private ArrayList<ProductionOrder> allOrders = null;

    @Test
    public void queueIsProperlyDetectingInProgressAndReadyAndCompletedOrders() {
        createWorld(4,
            () -> {
                if (A.now() == 1) frame1_queueIsInitializedFromBuildOrder();
                if (A.now() == 2) frame2_completedOrdersAreDetected();
                if (A.now() == 3) frame3_inProgressOrdersAreDetected();
            },
            () -> ourInitialUnits(),
            () -> fakeExampleEnemies(),
            Options.create().set("supplyUsed", 49)
        );
    }

    private void frame1_queueIsInitializedFromBuildOrder() {
        queue = initQueue();
        allOrders = buildOrder.productionOrders();

//        queue.allOrders().print("All orders");
//        queue.readyToProduceOrders().print("Ready to produce orders");

        assertEquals(allOrders.size(), queue.allOrders().size());
        assertEquals(0, queue.inProgressOrders().size());
        assertEquals(4, queue.readyToProduceOrders().size());
        assertEquals(0, queue.completedOrders().size());

        Queue.get().refresh(); // Refresh with no changes shouldn't change anything

        assertEquals(allOrders.size(), queue.allOrders().size());
        assertEquals(0, queue.inProgressOrders().size());
        assertEquals(4, queue.readyToProduceOrders().size());
        assertEquals(0, queue.completedOrders().size());
    }

    private void frame2_completedOrdersAreDetected() {
        mockOurUnitsByAddingNewUnit(fakeOurs(
            fake(Terran_Supply_Depot, 7)
        ));

        Queue.get().refresh();

        /* We expect this:
              At 9 SupplyD (COMPLETED)
              At 11 Barracks (READY_TO_PRODUCE)
              At 14 SupplyD (READY_TO_PRODUCE)
              At 18 Refinery (READY_TO_PRODUCE)
              At 19 Academy
              At 20 Bunker MAIN_CHOKE
              At 21 Barracks (READY_TO_PRODUCE)
              At 22 SupplyD (READY_TO_PRODUCE)
         */

//        queue.allOrders().print("\nAfter refreshing");

        assertEquals(0, queue.inProgressOrders().size());
        assertEquals(3, queue.readyToProduceOrders().size());
        assertEquals(1, queue.completedOrders().size());
    }

    private void frame3_inProgressOrdersAreDetected() {
        mockOurUnitsByAddingNewUnit(fakeOurs(
            fake(Terran_Supply_Depot, 7),
            fake(Terran_Barracks, 4).setCompleted(false),
            fake(Terran_Academy, 33).setCompleted(false)
        ));

        Queue.get().refresh();
//        queue.allOrders().print("\nAfter refreshing");

        assertEquals(1, queue.inProgressOrders().ofType(Terran_Barracks).size());
        assertEquals(2, queue.inProgressOrders().size());
        assertEquals(2, queue.readyToProduceOrders().size());
        assertEquals(1, queue.completedOrders().size());

        mockOurUnitsByAddingNewUnit(fakeOurs(
            fake(Terran_Supply_Depot, 7),
            fake(Terran_Barracks, 4).setCompleted(true),
            fake(Terran_Academy, 33).setCompleted(true)
        ));

        Queue.get().refresh();
//        queue.allOrders().print("\nAfter refreshing");

        assertEquals(3, queue.completedOrders().size());
        assertEquals(0, queue.inProgressOrders().ofType(Terran_Barracks).size());
        assertEquals(0, queue.inProgressOrders().size());
        assertEquals(7, queue.readyToProduceOrders().size()); // Why two medics aren't allowed here?
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

    private Queue initQueue() {
        return initQueue(3456, 2345);
    }

    private Queue initQueue(int minerals, int gas) {
        aGame.when(AGame::minerals).thenReturn(minerals);
        aGame.when(AGame::gas).thenReturn(gas);
        OurStrategy.setTo(TerranStrategies.TERRAN_MMG_vP);

        buildOrder = OurStrategy.get().buildOrder();
        initSupply();

        QueueInitializer.initializeProductionQueue();

        return queue = Queue.get();
    }

    public void initSupply() {
        int supplyFree = 2;
        int supplyUsed = options.getIntOr("supplyUsed", 49);

        aGame.when(AGame::supplyUsed).thenReturn(supplyUsed);
        aGame.when(AGame::supplyFree).thenReturn(supplyFree);
        aGame.when(AGame::supplyTotal).thenReturn(supplyUsed + supplyFree);
    }
}
