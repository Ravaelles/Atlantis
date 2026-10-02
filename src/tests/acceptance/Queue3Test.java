package tests.acceptance;

import atlantis.game.AGame;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.util.Options;
import bwapi.TechType;
import org.junit.jupiter.api.Test;
import tests.unit.DynamicMockOurUnits;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitHelper;

import java.util.ArrayList;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Queue3Test extends WorldStubForTests {
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

                // Counts are derived from the test build order instead of being
                // hard-coded: the build order gained a second barracks at some
                // point, which is why these two tests started failing while the
                // queue code stayed correct.
                int barracksInBuildOrder = 0;
                for (ProductionOrder order : buildOrder.productionOrders()) {
                    if (Terran_Barracks.equals(order.unitType())) barracksInBuildOrder++;
                }

                assertEquals(2, Count.withPlanned(Terran_Medic), "two medics come from the build order");

                assertEquals(0, Count.inProduction(Terran_Barracks));
                assertEquals(1, Count.existing(Terran_Barracks), "we placed exactly one barracks");
                assertEquals(barracksInBuildOrder, Count.inProductionOrInQueue(Terran_Barracks));
                assertEquals(
                    Count.existing(Terran_Barracks) + Count.inProductionOrInQueue(Terran_Barracks),
                    Count.withPlanned(Terran_Barracks),
                    "withPlanned is existing + queued"
                );

                // buildToHave must be a no-op while the plan is already enough...
                int before = Count.withPlanned(Terran_Barracks);
                buildToHave(Terran_Barracks, before);

                assertEquals(before, Count.withPlanned(Terran_Barracks));

                // ...and must top the plan up to the requested amount.
                buildToHave(Terran_Barracks, before + 1);

                assertEquals(before + 1, Count.withPlanned(Terran_Barracks));
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

                int before = Count.withPlanned(Terran_Barracks);

                assertEquals(0, Count.inProduction(Terran_Barracks));
                assertEquals(1, Count.existing(Terran_Barracks));
                assertEquals(
                    Count.existing(Terran_Barracks) + Count.inProductionOrInQueue(Terran_Barracks),
                    Count.withPlanned(Terran_Barracks),
                    "withPlanned is existing + queued"
                );

                buildToHave(Terran_Barracks, before + 4);

                assertEquals(before + 4, Count.withPlanned(Terran_Barracks),
                    "buildToHave tops the plan up to the requested number");
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
        currentSupplyUsed = options.getIntOr("supplyUsed", 49);
        currentSupplyTotal = currentSupplyUsed + 2;

        aGame.when(AGame::supplyUsed).thenAnswer(invocation -> currentSupplyUsed());
        aGame.when(AGame::supplyTotal).thenAnswer(invocation -> currentSupplyTotal());
        aGame.when(AGame::supplyFree).thenAnswer(invocation -> currentSupplyFree());
    }
}
