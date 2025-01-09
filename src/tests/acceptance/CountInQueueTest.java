package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Options;
import org.junit.jupiter.api.Test;
import tests.unit.DynamicMockOurUnits;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitHelper;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountInQueueTest extends WorldStubForTests {
    private ArrayList<ProductionOrder> allOrders = null;
    private FakeUnit newBunker = null;

    @Test
    public void bunkersInQueue() {
        createWorld(2,
            () -> {
                if (A.now() == 1) frame1();
                else if (A.now() == 2) frame2();
            },
            () -> FakeUnitHelper.merge(
                ourInitialUnits(),
                fakeOurs(
                    fake(Terran_Supply_Depot, 20),
                    fake(Terran_Barracks, 21),
                    fake(Terran_Academy, 22)
                )
            ),
            () -> fakeExampleEnemies(),
            Options.create().set("supplyUsed", 49)
        );
    }

    private void frame2() {
        assertEquals(0, Count.bunkers());
        assertEquals(0, CountInQueue.count(Terran_Bunker));
        assertEquals(0, CountInQueue.count(Terran_Bunker, 10));
        assertEquals(1, Count.bunkersWithUnfinished());
        assertEquals(1, Count.withPlanned(Terran_Bunker));

        mockOurUnitsByAddingNewUnit(fakeOurs(
            newBunker = fake(Terran_Bunker, 44).setCompleted(true)
        ));

        assertEquals(0, CountInQueue.count(Terran_Bunker));
        assertEquals(0, CountInQueue.count(Terran_Bunker, 10));
        assertEquals(1, Count.bunkers());
        assertEquals(1, Count.bunkersWithUnfinished());
        assertEquals(1, Count.withPlanned(Terran_Bunker));
    }

    private void frame1() {
        queue = initQueue();

//                queue.readyToProduceOrders.print("ReadyToProduceOrders");
//                Select.our().print("Ours");
//                Select.ourWithUnfinished().exclude(Select.our()).print("only our UNFINISHED");

        assertEquals(0, Count.bunkers());
        assertEquals(0, Count.bunkersWithUnfinished());
        assertEquals(1, CountInQueue.count(Terran_Bunker));
        assertEquals(1, Count.withPlanned(Terran_Bunker));
        assertEquals(1, Count.withPlanned(Terran_Bunker));

        mockOurUnitsByAddingNewUnit(fakeOurs(
            newBunker = fake(Terran_Bunker, 44).setCompleted(false)
        ));

//                queue.readyToProduceOrders().print("Now ready >>>>");
//                queue.allOrders().print("ALL >>>>");

        Count.clearCache();
        Select.clearCache();

        assertEquals(0, queue.readyToProduceOrders().ofType(Terran_Bunker).size());
        assertEquals(0, queue.nextOrders(50).ofType(Terran_Bunker).size());
        assertEquals(0, Count.bunkers());
        assertEquals(0, CountInQueue.count(Terran_Bunker));
        assertEquals(0, CountInQueue.count(Terran_Bunker, 10));
        assertEquals(1, Count.bunkersWithUnfinished());
        assertEquals(1, Count.withPlanned(Terran_Bunker));

        Queue.get().refresh();

        assertEquals(0, queue.readyToProduceOrders().ofType(Terran_Bunker).size());
        assertEquals(0, queue.nextOrders(50).ofType(Terran_Bunker).size());
        assertEquals(0, Count.bunkers());
        assertEquals(0, CountInQueue.count(Terran_Bunker));
        assertEquals(0, CountInQueue.count(Terran_Bunker, 10));
        assertEquals(1, Count.bunkersWithUnfinished());
        assertEquals(1, Count.withPlanned(Terran_Bunker));
    }

    // =========================================================

    private FakeUnit[] ourInitialUnits() {
        return fakeOurs(
//            fake(AUnitType.Terran_Missile_Turret, 8),
//            fake(AUnitType.Terran_Wraith, 9),
//            fake(AUnitType.Terran_Bunker, 10),
//            fake(AUnitType.Terran_Bunker, 11).setHp(0),
//            fake(AUnitType.Terran_Bunker, 12).setCompleted(false)
        );
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
