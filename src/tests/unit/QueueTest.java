package tests.unit;

import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.QueueFactory;
import atlantis.units.AUnitType;
import org.junit.Test;
import tests.acceptance.NonAbstractTestFakingGame;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class QueueTest extends NonAbstractTestFakingGame {
    private ABuildOrder buildOrder;

    //    @Test
//    public void fooBar() {
//        Queue queue = initQueue();
//
//        queue.inProgressOrders();
//        queue.readyToProduceOrders();
//        queue.completedOrders();
//        queue.allOrders(); // All orders in the queue
//
////        queue.inProgress();
////        queue.readyToProduce();
////        queue.completed();
////        queue.all(); // All orders in the queue
//    }

    @Test
    public void queueIsInitializedWithOrdersComingFromBuildOrder() {
        Queue queue = initQueue();

        assertEquals(buildOrder.productionOrders().size(), queue.allOrders().size());
        assertEquals(0, queue.inProgressOrders().size());
        assertEquals(0, queue.readyToProduceOrders().size());
        assertEquals(0, queue.completedOrders().size());
    }

    @Test
    public void whenBuildingIsBeingProducedItIsPresentInInProgressOrders() {
        Queue queue = initQueue();
        int allOrders = buildOrder.productionOrders().size();

        assertEquals(allOrders, queue.allOrders().size());

//        FakeUnit[] our = fakeOurs(
//            fake(AUnitType.Protoss_Dragoon, 11)
//        );
//        FakeUnit[] enemies = fakeEnemies();

        createWorld(1,
            () -> {
                Queue.get().update();

                assertEquals(allOrders, queue.allOrders().size());
                assertEquals(1, queue.inProgressOrders().size());
                assertEquals(0, queue.readyToProduceOrders().size());
                assertEquals(0, queue.completedOrders().size());
            },
            () -> fakeOurs(fake(AUnitType.Protoss_Dragoon, 11)),
            () -> fakeEnemies()
        );
    }

    private Queue initQueue() {
        buildOrder = OurStrategy.get().buildOrder();
        System.out.println("buildOrder = " + buildOrder);
//        Queue queue = BuildOrderToQueue.fromBuildOrder(buildOrder);
        Queue queue = QueueFactory.fromBuildOrder(buildOrder);
        queue.print();

        return queue;
    }
}
