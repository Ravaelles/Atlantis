package tests.unit;

import atlantis.game.listeners.OnOurUnitCreated;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Warp-to-order linking without the queue (NEXT #228, WO-147): a building
 * that warps off a v2 construction links the construction's synthetic
 * order directly - the queue fallback below only ever served legacy
 * rows. A construction-less building with an empty queue links nothing
 * (same as today, never worse).
 */
@Tag("production")
public class WarpOrderLinkTest extends WorldStubForTests {

    @AfterEach
    public void cleanQueue() {
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
    }

    @Test
    public void v2ConstructionLinksItsSyntheticOrder() {
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 20, 20);

        world(2, fakeOurs(pylon), fakeEnemies(), () -> {
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            ProductionOrder synthetic = new ProductionOrder(AUnitType.Protoss_Pylon, 0);
            construction.setProductionOrder(synthetic);
            pylon.setConstruction(construction);

            assertEquals(synthetic,
                OnOurUnitCreated.defineProductionOrderForUnit(pylon, null),
                "v2 warp links the stapled order, the queue is never consulted");
        });
    }

    @Test
    public void noConstructionAndEmptyQueueLinksNothing() {
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 20, 20);

        world(2, fakeOurs(pylon), fakeEnemies(), () -> {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            assertNull(OnOurUnitCreated.defineProductionOrderForUnit(pylon, null),
                "nothing linked - same as today");
        });
    }
}
