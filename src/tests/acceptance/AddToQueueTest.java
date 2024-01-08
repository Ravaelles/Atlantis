package tests.acceptance;

import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.util.Options;
import org.junit.Test;
import tests.unit.FakeUnit;
import tests.unit.FakeUnitHelper;

import static atlantis.units.AUnitType.*;
import static org.junit.Assert.*;

public class AddToQueueTest extends NonAbstractTestFakingGame {
    @Test
    public void marinesAreNotAddedMultipleTimesToTheQueue() {
        createWorld(1,
            () -> {
                queue = initQueue();

                ProductionOrder added;

                queue.nextOrders(15).print("nextOrders");
                assertEquals(0, queue.nextOrders(15).ofType(Terran_Marine).size());
                assertNotEquals(Terran_Marine, queue.nextOrders(1).list().get(0).unitType());

                added = AddToQueue.maxAtATime(Terran_Marine, 2);
                System.err.println("added = " + added);

                queue.clearCache();
                assertEquals(1, queue.nextOrders(15).ofType(Terran_Marine).size());
//                queue.nextOrders(15).print("nextOrders A");
                assertEquals(1, queue.nextOrders(15).ofType(Terran_Marine).size());
//                queue.readyToProduceOrders().print("ReadyToProduceOrders");

                added = AddToQueue.maxAtATime(Terran_Marine, 2);
//                System.err.println("added = " + added);

                queue.clearCache();
//                queue.nextOrders(15).print("nextOrders B");
                assertEquals(2, queue.nextOrders(15).ofType(Terran_Marine).size());

                queue.clearCache();
                added = AddToQueue.maxAtATime(Terran_Marine, 2);
//                System.err.println("added = " + added);

                queue.clearCache();
                assertEquals(2, queue.nextOrders(15).ofType(Terran_Marine).size());
//                queue.nextOrders(15).print("nextOrders");

                queue.clearCache();
                int medicsInQueue = queue.nextOrders(30).ofType(Terran_Medic).size();
                AddToQueue.maxAtATime(Terran_Medic, 5); // Now a Medic

                queue.clearCache();
                assertEquals(medicsInQueue + 1, queue.nextOrders(30).ofType(Terran_Medic).size());
//                queue.nextOrders(15).print("nextOrders");
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
            Options.create().set("supplyUsed", 33).set("supplyTotal", 44)
        );
    }

    // =========================================================

    private FakeUnit[] ourInitialUnits() {
        return fakeExampleOurs();
    }
}
