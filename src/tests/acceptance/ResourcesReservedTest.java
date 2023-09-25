package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.TerranStrategies;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.QueueInitializer;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.util.Options;
import org.junit.Test;
import tests.unit.DynamicMockOurUnits;
import tests.unit.FakeUnit;
import tests.unit.FakeUnitHelper;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResourcesReservedTest extends NonAbstractTestFakingGame {
    private Queue queue = null;
    private Orders readyToProduceOrders;
    private int initialReservedMinerals;
    private int afterInProgressMinerals;

    @Test
    public void reservedMineralsAndGasAreUpdatedAsOrderStatusChanges() {
        ReservedResources.reset();
        initialReservedMinerals = 550;
        afterInProgressMinerals = 450;

        createWorld(1,
            () -> {
                mineralsAreReservedForOrdersMarkedAsReady();
                ProductionOrder order = readyToProduceOrders.first();

                assertEquals(initialReservedMinerals, ReservedResources.minerals());

//                System.out.println("readyToProduceOrders.first() = " + readyToProduceOrders.first());
//                readyToProduceOrders.first().unitType().print("First unit type");

//                Queue.get().allOrders().print("Before in progress");

                order.setStatus(OrderStatus.IN_PROGRESS);

//                Queue.get().allOrders().print("After in progress");

                assertEquals(afterInProgressMinerals, ReservedResources.minerals());

                order.setStatus(OrderStatus.COMPLETED);

//                Queue.get().allOrders().print("After completed");

                assertEquals(afterInProgressMinerals, ReservedResources.minerals());
            },
            () -> FakeUnitHelper.merge(
                ourInitialUnits(),
                fakeOurs(
                    fake(Terran_Supply_Depot, 7),
                    fake(Terran_Barracks, 4),
                    fake(Terran_Academy, 33)
                )
            ),
            () -> fakeExampleEnemies(),
            Options.create().set("supplyUsed", 49)
        );
    }

    private void mineralsAreReservedForOrdersMarkedAsReady() {
//        ReservedResources.reset();

//        assertEquals(0, ReservedResources.minerals());

        queue = initQueue(640, 2323);
        queue.refresh();

        assertEquals(640, A.minerals());

        readyToProduceOrders = queue.readyToProduceOrders();

//        queue.allOrders().print("All orders");
//        readyToProduceOrders.print("ReadyToProduceOrders");
//        ReservedResources.print();

        assertEquals(initialReservedMinerals, ReservedResources.minerals());
//        assertEquals(6, readyToProduceOrders.size());
        Orders nextOrders = queue.nextOrders(20);

//        queue.allOrders().print("All orders");
//        nextOrders.print("\nNext orders");

        assertTrue(readyToProduceOrders.size() < nextOrders.size());
        assertTrue(nextOrders.first().minSupply() >= 14);
    }

    // =========================================================

    private FakeUnit[] ourInitialUnits() {
        return fakeExampleOurs();
    }

    private Queue initQueue(int minerals, int gas) {
        aGame.when(AGame::minerals).thenReturn(minerals);
        aGame.when(AGame::gas).thenReturn(gas);

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
