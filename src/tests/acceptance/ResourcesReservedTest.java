package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.TerranStrategies;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.QueueInitializer;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Select;
import atlantis.util.Options;
import bwapi.TechType;
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

    @Test
    public void reservedMineralsAndGasAreRespected() {
        ReservedResources.reset();

        createWorld(1,
            () -> {
                ReservedResources.reset();

                assertEquals(0, ReservedResources.minerals());

                queue = initQueue(640, 2323);
                queue.refresh();

                assertEquals(640, A.minerals());

                Orders readyToProduceOrders = queue.readyToProduceOrders();

//                queue.allOrders().print("All orders");
//                readyToProduceOrders.print("ReadyToProduceOrders");

                assertEquals(600, ReservedResources.minerals());
                assertEquals(6, readyToProduceOrders.size());
                Orders nextOrders = queue.nextOrders(20);
                int nextOrdersSize = nextOrders.size();

//                queue.allOrders().print("All orders");
//                nextOrders.print("\nNext orders");

                assertTrue(readyToProduceOrders.size() < nextOrdersSize);
                assertEquals(11, nextOrdersSize);
                assertTrue(nextOrders.list().get(nextOrdersSize - 1).minSupply() >= 45);
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

        aGame.when(AGame::supplyUsed).thenReturn(supplyUsed);
        aGame.when(AGame::supplyFree).thenReturn(supplyFree);
        aGame.when(AGame::supplyTotal).thenReturn(supplyUsed + supplyFree);
    }
}
