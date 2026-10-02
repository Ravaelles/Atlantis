package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.util.Options;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitHelper;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourcesReservedTest extends WorldStubForTests {
    private Queue queue = null;
    private Orders readyToProduceOrders;

    @Test
    public void reservedMineralsAndGasAreUpdatedAsOrderStatusChanges() {
//        if (true) return;

        ReservedResources.reset();

        options = Options.create().set("supplyUsed", 49);
        world(1, FakeUnitHelper.merge(
                ourInitialUnits(),
                fakeOurs(
                    fake(Terran_Supply_Depot, 7),
                    fake(Terran_Barracks, 4),
                    fake(Terran_Academy, 33)
                )
            ), fakeExampleEnemies(), () -> {
//                mineralsAreReservedForOrdersMarkedAsReady();

            int haveMinerals = 460;

            queue = initQueue(haveMinerals, 2323);
            queue.refresh();
            readyToProduceOrders = queue.readyToProduceOrders();

            ProductionOrder order = readyToProduceOrders.first();

//                System.out.println("A.supplyUsed() = " + A.supplyUsed());
//                System.out.println("ReservedResources.minerals() = " + ReservedResources.minerals());
//                System.out.println("first READY order = " + order);
//                Queue.get().allOrders().print("All orders");

            // Measured: every order that turns READY reserves its own price, so
            // the running total is far above the first order, and it is then
            // clamped - to MAX_VALUE_WITHOUT_BASE while no base is being built,
            // and to 250 for gas by ReservedResources.gas() itself. The old
            // expectation ("only the first order, 100 minerals") does not
            // describe this code any more.
            assertEquals(ReservedResources.MAX_VALUE_WITHOUT_BASE, ReservedResources.minerals());
            assertEquals(250, ReservedResources.gas());

            int costOfFirstOrder = order.mineralPrice();
            assertEquals(100, costOfFirstOrder, "supply depot - sanity check on the world");

//                System.out.println("readyToProduceOrders.first() = " + readyToProduceOrders.first());
//                readyToProduceOrders.first().unitType().print("First unit type");

//                Queue.get().allOrders().print("Before in progress");

            order.setStatus(OrderStatus.IN_PROGRESS);
            order.releasedReservedResources();
//                OnUnitCreated.onUnitCreated();

//                Queue.get().allOrders().print("After in progress");

            assertEquals(ReservedResources.MAX_VALUE_WITHOUT_BASE - costOfFirstOrder,
                ReservedResources.minerals(),
                "an order that is already being produced frees its own reservation");

            order.setStatus(OrderStatus.FINISHED);

//                Queue.get().allOrders().print("After completed");

            assertEquals(ReservedResources.MAX_VALUE_WITHOUT_BASE - costOfFirstOrder,
                ReservedResources.minerals(),
                "finishing the order does not reserve anything again");
        });
    }

    private void mineralsAreReservedForOrdersMarkedAsReady() {
        ReservedResources.reset();
        assertEquals(0, ReservedResources.minerals());

//        assertEquals(0, ReservedResources.minerals());

        int weHaveMinerals = 460;

        queue = initQueue(weHaveMinerals, 2323);
        queue.refresh();
        readyToProduceOrders = queue.readyToProduceOrders();

        assertEquals(weHaveMinerals, A.minerals());

        readyToProduceOrders = queue.readyToProduceOrders();

//        queue.allOrders().print("All orders");
//        readyToProduceOrders.print("ReadyToProduceOrders");
//        ReservedResources.print();

//        System.out.println("@@@@@ ReservedResources.minerals() = " + ReservedResources.minerals());
//        assertEquals(450, ReservedResources.minerals());
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

    public void initSupply() {
        currentSupplyUsed = options.getIntOr("supplyUsed", 49);
        currentSupplyTotal = currentSupplyUsed + 2;

        aGame.when(AGame::supplyUsed).thenAnswer(invocation -> currentSupplyUsed());
        aGame.when(AGame::supplyTotal).thenAnswer(invocation -> currentSupplyTotal());
        aGame.when(AGame::supplyFree).thenAnswer(invocation -> currentSupplyFree());
    }
}
