package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.builders.RefreshConstructionPosition;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NEXT #145.1: a standing-covered site resites at issue time, it is not
 * cancelled. Cancelling + re-requesting the same center loops forever
 * against a standing sibling (street-grid adjacency - measured: same-tile
 * Gateway re-picks, Pylon re-fires, the [105,115] chase). The adopt door
 * already resites this shape (adopt-resited); the issue door mirrors it.
 *
 * <p>Red-first: the covered site was cancelled and re-requested at the
 * same tile.</p>
 *
 * <p>Setup notes (each clause is load-bearing): the builder is committed
 * (on the site) but stuck (motionless 240+ frames, fired at 291) - a live
 * walk is never yanked (#53), only a dead one is re-sited. The site is
 * explored (unexplored fails for other reasons). Pylon needs no power, so
 * the random resite tile passes the power gate.</p>
 */
public class IssueRefreshResitesCoveredSiteTest extends WorldStubForTests {

    private static final int SX = 86;
    private static final int SY = 118;

    private boolean fired = false;

    private static int pylonOrdersInQueue() {
        int total = 0;
        for (ProductionOrder order : Queue.get().allOrders().list()) {
            if (order.unitType() != null
                && order.unitType().equals(AUnitType.Protoss_Pylon)) {
                total++;
            }
        }
        return total;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        fired = false;
    }

    @Test
    public void standingCoveredSiteResitesInsteadOfCancelling() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, SX, SY);
        FakeUnit standingPylon = fake(AUnitType.Protoss_Pylon, SX, SY);
        standingPylon.setCompleted(true);

        world(300, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            builder,
            standingPylon
        ), fakeEnemies(), () -> {
            // 291 = 3rd issue-time refresh tick (everyNthGameFrame(97));
            // the motionless builder has been stuck 291 frames (> 240).
            if (atlantis.game.A.now() != 291 || fired) return;

            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            currentMinerals = 400;
            currentGas = 100;
            tests.fakes.FakeMapTiles.EXPLORED = true;

            APosition site = APosition.create(SX, SY);
            Construction waiting = new Construction(AUnitType.Protoss_Pylon);
            waiting.setProductionOrder(new ProductionOrder(AUnitType.Protoss_Pylon, 8));
            waiting.setPositionToBuild(site);
            // Park the builder on the site and freeze its movement clock:
            // committed (dist 0) but stuck (motionless 291 frames) - a live
            // walk is never yanked (#53), only a dead one is re-sited.
            builder.position = site;
            builder.unitState().setLastPositionChanged(0);
            waiting.setBuilder(builder);
            ConstructionRequests.addConstruction(waiting);

            boolean failed = RefreshConstructionPosition.finalRefreshBeforeIssuingOrderFailed(
                builder, waiting, AUnitType.Protoss_Pylon, site, builder);

            assertFalse(failed,
                "a standing-covered site resites - only a site with nowhere to go cancels");
            assertTrue(
                ConstructionRequests.constructions.contains(waiting),
                "resited, not cancelled: the construction stays queued");
            assertNotEquals(site, waiting.buildPosition(),
                "the site moves off the covered tile instead of re-requesting it");
            assertEquals(0, pylonOrdersInQueue(),
                "no same-tile re-request is enqueued");
        });

        assertTrue(fired, "the 291-tick refresh ran");
    }
}
