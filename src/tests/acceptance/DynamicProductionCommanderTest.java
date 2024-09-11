package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.dynamic.DynamicProductionCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Counter;
import atlantis.util.Options;
import org.junit.Test;
import tests.unit.DynamicMockOurUnits;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitHelper;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;
import static org.junit.Assert.assertEquals;

public class DynamicProductionCommanderTest extends NonAbstractTestFakingGame {
    private int nextX = 20;
    private ArrayList<FakeUnit> newUnits = new ArrayList<>();

    @Test
    public void testBuildingsProducedMakeSense() {
        createWorld(200,
            () -> {
                onFrame();
            },
            () -> FakeUnitHelper.merge(
                ourInitialUnits(),
                fakeOurs(
                    fake(Terran_Barracks, 4),
                    fake(Terran_Barracks, 5),
                    fake(Terran_Supply_Depot, 6),
                    fake(Terran_Supply_Depot, 7)
//                    fake(Terran_Academy, 33)
                )
            ),
            () -> fakeExampleEnemies(),
            Options.create().set("supplyUsed", 49)
        );
    }

    // =========================================================

    private void onFrame() {
        if (A.now() == 1) initFrame();
        else standardFrame();

        if (A.everyNthGameFrame(13)) {
            addOurNewUnits(fakeOurs(
                fake(Terran_Supply_Depot, nextX++)
            ));
        }

        if (A.everyNthGameFrame(9)) mockProductionAndProduceReadyUnits();

        if (A.everyNthGameFrame(10)) {
//            System.err.println("@ " + A.now() + " - SUPPLY = " + A.supplyUsed());
//            printOurUnitsSummary();
//            Queue.get().nonCompleted().print();
        }
    }

    private void standardFrame() {
        (new DynamicProductionCommander()).forceHandle();
    }

    private void initFrame() {
        initSupply(4, 10);
        aGame.when(AGame::minerals).thenReturn(579);
        aGame.when(AGame::gas).thenReturn(680);
    }

    // =========================================================

    private FakeUnit[] ourInitialUnits() {
        return fakeExampleOurs();
    }

    private void addOurNewUnits(FakeUnit[] ourNewFakeUnits) {
        newUnits.addAll(FakeUnitHelper.fakeUnitsToArrayList(ourNewFakeUnits));

        ArrayList<FakeUnit> ourUnits = FakeUnitHelper.fakeUnitsToArrayList(ourInitialUnits());
        ourUnits.addAll(newUnits);

        BaseSelect.clearCache();
        Select.clearCache();
        Count.clearCache();
        Queue.get().clearCache();

        int supplyTotal = 10 + 8 * Select.ourOfType(Terran_Supply_Depot).size();
        int supplyUsed = supplyTotal * 2 / 3;
        initSupply(supplyUsed, supplyTotal);

        DynamicMockOurUnits.mockOur(ourUnits);

        Queue.get().refresh();
    }

    private void printOurUnitsSummary() {
//        Select.our().print("Our @ " + A.now());

        Counter<AUnitType> counter = new Counter<>();
        for (AUnit unit : Select.our().list()) {
            counter.incrementValueFor(unit.type());
        }

        for (AUnitType type : counter.keys()) {
            A.println(type + ": " + counter.getValueFor(type));
        }
    }

    private void mockProductionAndProduceReadyUnits() {
        for (ProductionOrder order : Queue.get().readyToProduceOrders().list()) {
            AUnitType type = order.unitType();
            if (type != null) {
                newUnits.add(fake(type));
//                A.errPrintln("Producing " + type);
            }
        }
    }
}
