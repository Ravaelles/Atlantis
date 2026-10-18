package tests.unit;

import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Without a single worker nothing can be built, so the queue refresh is
 * a no-op (owner: WINE_DECISIONS showed BUILD attempts with zero workers,
 * and listener-driven refreshes twinned ORDER_NOT_READY for Pylon rows
 * that can never build). Training needs no worker either, but with zero
 * workers no producer issues anything, so nothing is lost.
 */
public class QueueWorkerlessGateTest extends AbstractTestWithWorld {

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
        EventLog.clearForTests();
    }

    @Test
    public void workerlessRefreshIsSilent() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);

        world(5, fakeOurs(nexus), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Pylon, 0));
            EventLog.clearForTests();

            Queue.get().refresh();

            assertFalse(
                EventLog.toJsonLines().contains("\"key\":\"ORDER_NOT_READY\""),
                "zero workers: no readiness churn, no twin rows");
            assertTrue(
                ConstructionRequests.constructions.isEmpty(),
                "zero workers: nothing sited");
        });
    }
}
