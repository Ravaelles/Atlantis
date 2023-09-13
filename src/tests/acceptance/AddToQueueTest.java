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

public class AddToQueueTest extends NonAbstractTestFakingGame {

    private Queue queue;

    @Test
    public void marinesAreNotAddedMultipleTimesToTheQueue() {
        createWorld(1,
            () -> {
                queue = initQueue();

                queue.readyToProduceOrders().print("ReadyToProduceOrders");

                AddToQueue.maxAtATime(Terran_Marine, 2);

                queue.clearCache();
                queue.nextOrders(15).print("nextOrders");
                queue.readyToProduceOrders().print("ReadyToProduceOrders");

                AddToQueue.maxAtATime(Terran_Marine, 2);

                queue.clearCache();
                queue.nextOrders(15).print("nextOrders");
                queue.readyToProduceOrders().print("ReadyToProduceOrders");

                AddToQueue.maxAtATime(Terran_Marine, 2);

                queue.clearCache();
                queue.readyToProduceOrders().print("ReadyToProduceOrders");

//                System.err.println("supplyUsed = " + AGame.supplyUsed());
//                System.err.println("supplyTotal = " + AGame.supplyTotal());

//                readyToProduceOrders.print("ReadyToProduceOrders");

//                assertEquals(2, readyToProduceOrders.ofType(Terran_Medic).size());
//                assertEquals(1, readyToProduceOrders.techType(TechType.Stim_Packs).size());
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
