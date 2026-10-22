package tests.unit;

import atlantis.config.env.Env;
import atlantis.config.env.ProductionV2Mode;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Single policy in LIVE: the legacy queue refreshes nothing - no maturing
 * rows, no prepared sites, no readiness rows. The orphan PREPARING with a
 * builder and the leaked reservations measured in LIVE games came from
 * exactly this path firing on unit events.
 */
@Tag("production")
public class QueueLiveGateTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void cleanAll() {
        ConstructionRequests.constructions.clear();
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        Env.setProductionV2ForTest(ProductionV2Mode.OFF);
    }

    @Test
    public void liveRefreshIsANoOp() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Pylon, 0));
            int orders = Queue.get().allOrders().list().size();

            Env.setProductionV2ForTest(ProductionV2Mode.LIVE);
            Queue.get().refresh();

            assertEquals(orders, Queue.get().allOrders().list().size());
            assertEquals(0, ConstructionRequests.constructions.size());
        });
    }
}
