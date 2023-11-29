package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.TerranStrategies;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.production.queue.QueueInitializer;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.util.Options;
import bwapi.TechType;
import org.junit.Test;
import tests.unit.DynamicMockOurUnits;
import tests.unit.FakeUnit;
import tests.unit.FakeUnitHelper;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class Queue1Test extends NonAbstractTestFakingGame {
    private ArrayList<ProductionOrder> allOrders = null;

    @Test
    public void queueIsInitializedWithOrdersComingFromBuildOrder() {
        createWorld(1,
            () -> {
                queue = initQueue();

//                aGame.when(AGame::supplyUsed).thenReturn(10);
//                aGame.when(AGame::supplyTotal).thenReturn(18);
//                aGame.when(AGame::minerals).thenReturn(88);
//                aGame.when(AGame::gas).thenReturn(66);

//                queue.readyToProduceOrders().print("Ready to produce orders");
//                queue.inProgressOrders().print("In progress orders");

                assertEquals(buildOrder.productionOrders().size(), queue.allOrders().size());
                assertEquals(0, queue.inProgressOrders().size());
                assertEquals(0, queue.readyToProduceOrders().size());
                assertEquals(0, queue.completedOrders().size());
            },
            () -> ourInitialUnits(),
            () -> fakeExampleEnemies(),
            Options.create().set("supplyUsed", 8)
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

    public void initSupply() {
        int supplyFree = 2;
        int supplyUsed = options.getIntOr("supplyUsed", 49);

        aGame.when(AGame::supplyUsed).thenReturn(supplyUsed);
        aGame.when(AGame::supplyFree).thenReturn(supplyFree);
        aGame.when(AGame::supplyTotal).thenReturn(supplyUsed + supplyFree);
    }
}
