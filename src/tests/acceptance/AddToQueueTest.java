package tests.acceptance;

import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.TerranStrategies;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.build.AddToQueue;
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
import static org.junit.Assert.assertNotEquals;

public class AddToQueueTest extends NonAbstractTestFakingGame {
    private Queue queue;

    @Test
    public void marinesAreNotAddedMultipleTimesToTheQueue() {
        createWorld(1,
            () -> {
                queue = initQueue();

                queue.nextOrders(15).print("nextOrders");
                assertEquals(0, queue.nextOrders(15).ofType(Terran_Marine).size());
                assertNotEquals(Terran_Marine, queue.nextOrders(1).list().get(0).unitType());

                AddToQueue.maxAtATime(Terran_Marine, 2);

                queue.clearCache();
                assertEquals(1, queue.nextOrders(15).ofType(Terran_Marine).size());
                queue.nextOrders(15).print("nextOrders");
                assertEquals(Terran_Marine, queue.nextOrders(1).list().get(0).unitType());
//                queue.readyToProduceOrders().print("ReadyToProduceOrders");

                AddToQueue.maxAtATime(Terran_Marine, 2);

                queue.clearCache();
                queue.nextOrders(15).print("nextOrders");
                assertEquals(2, queue.nextOrders(15).ofType(Terran_Marine).size());
                assertEquals(Terran_Marine, queue.nextOrders(2).list().get(0).unitType());
                assertEquals(Terran_Marine, queue.nextOrders(2).list().get(1).unitType());

                AddToQueue.maxAtATime(Terran_Marine, 2);

                queue.clearCache();
                assertEquals(2, queue.nextOrders(15).ofType(Terran_Marine).size());
                assertNotEquals(Terran_Marine, queue.nextOrders(3).list().get(2).unitType());
                queue.nextOrders(15).print("nextOrders");
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

    private Queue initQueue() {
        return initQueue(3456, 2345);
    }

    private Queue initQueue(int minerals, int gas) {
        aGame.when(AGame::minerals).thenReturn(minerals);
        aGame.when(AGame::gas).thenReturn(gas);
        OurStrategy.setTo(TerranStrategies.TERRAN_MMG_vP);

        initSupply();

        QueueInitializer.initializeProductionQueue();

        return queue = Queue.get();
    }

    public void initSupply() {
        int supplyFree = 2;
        int supplyUsed = options.getIntOr("supplyUsed", 49);
        int supplyTotal = options.getIntOr("supplyTotal", supplyUsed + supplyFree);

        aGame.when(AGame::supplyUsed).thenReturn(supplyUsed);
        aGame.when(AGame::supplyFree).thenReturn(supplyFree);
        aGame.when(AGame::supplyTotal).thenReturn(supplyTotal);
    }
}
