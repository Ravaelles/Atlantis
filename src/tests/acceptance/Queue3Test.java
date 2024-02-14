package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Options;
import bwapi.TechType;
import org.junit.Test;
import tests.unit.DynamicMockOurUnits;
import tests.unit.FakeUnit;
import tests.unit.FakeUnitHelper;

import java.util.ArrayList;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

import static atlantis.units.AUnitType.*;
import static org.junit.Assert.assertEquals;

public class Queue3Test extends NonAbstractTestFakingGame {
    private ArrayList<ProductionOrder> allOrders = null;

    @Test
    public void medicsAndStimpacksAreIdentifiedAsReady() {
        createWorld(1,
            () -> {
                queue = initQueue();
                Orders readyToProduceOrders = queue.readyToProduceOrders();

//                Select.our().print("Our units");
//                queue.allOrders().print("Initial orders");
//                readyToProduceOrders.print("ReadyToProduceOrders");

                assertEquals(2, readyToProduceOrders.ofType(Terran_Medic).size());
                assertEquals(1, readyToProduceOrders.techType(TechType.Stim_Packs).size());
            },
            () -> FakeUnitHelper.merge(
                ourInitialUnits(),
                fakeOurs(
                    fake(Terran_Barracks, 4),
                    fake(Terran_Supply_Depot, 5),
                    fake(Terran_Supply_Depot, 6),
                    fake(Terran_Supply_Depot, 7),
                    fake(Terran_Supply_Depot, 8),
                    fake(Terran_Refinery, 29),
                    fake(Terran_Academy, 33)
                )
            ),
            () -> fakeExampleEnemies(),
            Options.create().set("supplyUsed", 49)
        );
    }

    @Test
    public void buildingsInQueueAreCounted() {
        createWorld(1,
            () -> {
                queue = initQueue();
                Orders readyToProduceOrders = queue.readyToProduceOrders();

//                readyToProduceOrders.print("ReadyToProduceOrders");
//                queue.allOrders().print("Initial orders");
//                queue.completedOrders().print("Completed");

                assertEquals(2, Count.withPlanned(Terran_Medic));

                assertEquals(0, Count.inProduction(Terran_Barracks));
                assertEquals(1, Count.existing(Terran_Barracks));
                assertEquals(1, Count.inProductionOrInQueue(Terran_Barracks));
                assertEquals(2, Count.withPlanned(Terran_Barracks));

                buildToHave(Terran_Barracks, 2);

                assertEquals(2, Count.withPlanned(Terran_Barracks));

                buildToHave(Terran_Barracks, 3);

                assertEquals(3, Count.withPlanned(Terran_Barracks));
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

    @Test
    public void buildToHaveMultiple() {
        createWorld(1,
            () -> {
                queue = initQueue();
                Orders readyToProduceOrders = queue.readyToProduceOrders();

//                readyToProduceOrders.print("ReadyToProduceOrders");
//                queue.allOrders().print("Initial orders");
//                queue.completedOrders().print("Completed");

                assertEquals(2, Count.withPlanned(Terran_Medic));

                assertEquals(0, Count.inProduction(Terran_Barracks));
                assertEquals(1, Count.existing(Terran_Barracks));
                assertEquals(1, Count.inProductionOrInQueue(Terran_Barracks));
                assertEquals(2, Count.withPlanned(Terran_Barracks));

                buildToHave(Terran_Barracks, 8);

                assertEquals(8, Count.withPlanned(Terran_Barracks));
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

    public void initSupply() {
        int supplyFree = 2;
        int supplyUsed = options.getIntOr("supplyUsed", 49);

        aGame.when(AGame::supplyUsed).thenReturn(supplyUsed);
        aGame.when(AGame::supplyFree).thenReturn(supplyFree);
        aGame.when(AGame::supplyTotal).thenReturn(supplyUsed + supplyFree);
    }
}
