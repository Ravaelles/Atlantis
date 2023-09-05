package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.orders.production.CurrentProductionQueue;
import atlantis.production.orders.production.ProductionOrder;
import atlantis.production.orders.production.ProductionQueueMode;
import atlantis.units.AUnitType;
import org.junit.Test;
import tests.unit.FakeUnit;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CurrentProductionQueueTest extends NonAbstractTestFakingGame {
    private static ArrayList<ProductionOrder> queue;
    private FakeUnit depot;

    @Test
    public void productionQueueIsWorkingAsExpected() {
        FakeUnit[] our = fakeOurs(
            fake(AUnitType.Terran_Command_Center, 10),
            fake(Terran_Barracks, 11),
            fake(Terran_Engineering_Bay, 12),
            fake(AUnitType.Terran_SCV, 1),
            depot = fake(AUnitType.Terran_Supply_Depot, 18)
        );
        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Hatchery, 40)
        );

        createWorld(5, () -> {
                initSupply();

                if (A.now() == 1) firstFrame();
                else if (A.now() == 2) secondFrame();
            },
            () -> our,
            () -> enemies
        );
    }

    // =========================================================

    private void firstFrame() {
        assertEquals(444, AGame.minerals());
        assertEquals(333, AGame.gas());

        assertQueueHasListOfOrdersComingFromTheBuildOrder();
        assertQueueReturnsOrdersWeCanProduceNowDependingOnRequirements();

        requestMissileTurret();
    }

    private void secondFrame() {
        queue = CurrentProductionQueue.get(ProductionQueueMode.REQUIREMENTS_FULFILLED);

//        A.printList(queue);
        assertEquals(7, queue.size());
    }

    // =========================================================

    private void requestMissileTurret() {
        AddToQueue.withHighPriority(Terran_Missile_Turret, depot.position());
    }

    private void initSupply() {
        aGame.when(AGame::supplyTotal).thenReturn(26);
        aGame.when(AGame::supplyUsed).thenReturn(23);
        aGame.when(AGame::supplyFree).thenReturn(3);
    }

    private static void assertQueueReturnsOrdersWeCanProduceNowDependingOnRequirements() {
//        CurrentProductionQueue.print(null);

        queue = CurrentProductionQueue.get(ProductionQueueMode.REQUIREMENTS_FULFILLED);

//        A.printList(queue);
        assertEquals(6, queue.size());
        assertEquals(Terran_Supply_Depot, queue.get(0).unitType());
    }

    private static void assertQueueHasListOfOrdersComingFromTheBuildOrder() {
        assertFalse(CurrentProductionQueue.get(ProductionQueueMode.ENTIRE_QUEUE).isEmpty());
    }
}
