package tests.acceptance;

import atlantis.game.AGame;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.util.Options;
import org.junit.jupiter.api.Test;
import tests.unit.DynamicMockOurUnits;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitHelper;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Queue1Test extends WorldStubForTests {
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

//                Select.our().print("Ours");
//                queue.readyToProduceOrders().print("Ready to produce orders");
//                queue.inProgressOrders().print("In progress orders");

                assertEquals(buildOrder.productionOrders().size(), queue.allOrders().size());
                assertEquals(0, queue.inProgressOrders().size());
//                assertEquals(0, queue.readyToProduceOrders().size());
                assertEquals(0, queue.finishedOrders().size());
                assertEquals(true, !queue.notFinished().isEmpty());
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
        currentSupplyUsed = options.getIntOr("supplyUsed", 49);
        currentSupplyTotal = currentSupplyUsed + 2;

        aGame.when(AGame::supplyUsed).thenAnswer(invocation -> currentSupplyUsed());
        aGame.when(AGame::supplyTotal).thenAnswer(invocation -> currentSupplyTotal());
        aGame.when(AGame::supplyFree).thenAnswer(invocation -> currentSupplyFree());
    }
}
