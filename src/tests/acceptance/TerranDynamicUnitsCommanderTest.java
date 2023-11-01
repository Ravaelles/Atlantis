package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.TerranStrategies;
import atlantis.production.dynamic.terran.TerranDynamicUnitsCommander;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.QueueInitializer;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Options;
import org.junit.Test;
import tests.unit.FakeUnit;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TerranDynamicUnitsCommanderTest extends NonAbstractTestFakingGame {
    private ABuildOrder buildOrder;
    private Queue queue = null;
    private ArrayList<ProductionOrder> allOrders = null;

    @Test
    public void letsSee() {
        FakeUnit[] ours = fakeOurs(
            fake(AUnitType.Terran_SCV),
            fake(AUnitType.Terran_Command_Center),
            fake(AUnitType.Terran_Comsat_Station),
            fake(AUnitType.Terran_Refinery),
            fake(AUnitType.Terran_Academy),
            fake(AUnitType.Terran_Barracks),

            fake(AUnitType.Terran_Medic),
            fake(AUnitType.Terran_Medic),

            fake(AUnitType.Terran_Marine),
            fake(AUnitType.Terran_Marine),
            fake(AUnitType.Terran_Marine),
            fake(AUnitType.Terran_Marine)
        );

        createWorld(1,
            () -> {
                queue = initQueue();

                (new TerranDynamicUnitsCommander()).invoke();

                Select.our().print("All our units!");
                System.out.println("## Supply: " + A.supplyUsed() + " \\ Minerals: " + A.minerals() + " ##\n");

                queue.allOrders().print("Queue all");
                Orders dynamicUnitOrders = queue.forCurrentSupply().dynamic().units();

                assertEquals(1, dynamicUnitOrders.size());
                assertEquals(true, dynamicUnitOrders.get(0).unitType().isMedic());
            },
            () -> ours,
            () -> fakeExampleEnemies(),
            Options.create().set("supplyUsed", 24)
        );
    }

    // =========================================================

    private Queue initQueue() {
        return initQueue(3456, 2345);
    }

    private Queue initQueue(int minerals, int gas) {
        aGame.when(AGame::minerals).thenReturn(minerals);
        aGame.when(AGame::gas).thenReturn(gas);
        OurStrategy.setTo(TerranStrategies.TERRAN_Tests);

        buildOrder = OurStrategy.get().buildOrder();
        initSupply();

        QueueInitializer.initializeProductionQueue();

        return queue = Queue.get();
    }

    private FakeUnit[] ourInitialUnits() {
        return fakeExampleOurs();
    }
}
