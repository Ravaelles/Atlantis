package tests.unit;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Without a single worker on a baseless map every building row is
 * structurally unbuildable, so the refresh is a no-op and the readiness
 * twin stays silent (owner: WINE_DECISIONS showed BUILD attempts with
 * zero workers; the WINE_DEBUG WUT stack showed the game-start BO load
 * plus every unit event running the legacy APositionFinder chain for
 * demand that can never build). Gated on baselessness too: a base that
 * merely lost its workers keeps the adopt-and-wait path
 * (PreparingConstructionSiteTest pins it).
 */
@Tag("production")
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
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        EventLog.clearForTests();
    }

    @Test
    public void workerlessRefreshIsSilent() {
        // Baseless UMS micro map: no workers, no Nexus. The game-start
        // BO load plus every unit event used to run the full legacy
        // chain here (WINE_DEBUG WUT stack via APositionFinder) for
        // demand that can never build. A base that merely lost its
        // workers keeps the adopt-and-wait path (pinned separately).
        world(5, fakeOurs(), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Pylon, 0));
            EventLog.clearForTests();

            Queue.get().refresh();

            assertFalse(
                EventLog.toJsonLines().contains("\"key\":\"ORDER_NOT_READY\""),
                "zero workers: refuse silently, no twin rows for rows that"
                + " can never build");
        });
    }
}
