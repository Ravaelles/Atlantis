package tests.acceptance;

import atlantis.production.dynamic.terran.TerranDynamicUnitsCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.Options;
import org.junit.Test;
import tests.unit.FakeUnit;
import tests.unit.FakeUnitData;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TerranDynamicUnitsCommanderTest extends NonAbstractTestFakingGame {
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

                int oldTrainUnits = FakeUnitData.TRAIN.size();

                (new TerranDynamicUnitsCommander()).invokeCommander();

//                Select.our().print("All our units!");
//                System.out.println("## Supply: " + A.supplyUsed() + " / Minerals: " + A.minerals() + " ##\n");
//                queue.allOrders().print("Queue all");

                Queue.get().clearCache();
                Orders dynamicUnitOrders = queue.forCurrentSupply().dynamic().units();

                int newTrainUnits = FakeUnitData.TRAIN.size();

                assertEquals(true, newTrainUnits > oldTrainUnits);
//                assertEquals(true, dynamicUnitOrders.get(0).unitType().isTerranInfantry());
            },
            () -> ours,
            () -> fakeExampleEnemies(),
            Options.create().set("supplyUsed", 24)
        );
    }

    // =========================================================

    private FakeUnit[] ourInitialUnits() {
        return fakeExampleOurs();
    }
}
