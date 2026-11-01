package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NEXT #145.1: overlap with a standing sibling is live street-grid
 * demand at a shifted site, not a fulfilled duplicate - resite instead
 * of absorbing (which silently dropped the demand) or cancelling (which
 * churned re-requests). Exact-tile duplicates still absorb
 * ({@code StaleDuplicateFinishesOnItsTilesTest}); a travelling builder
 * keeps its walk (#53).
 */
public class SiblingCoverResitesInsteadOfFinishingTest extends WorldStubForTests {

    private boolean fired = false;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        fired = false;
    }

    @Test
    public void siblingOverlapResitesInsteadOfFinishing() {
        // Standing Pylon at (87,118), footprint [87..88]x[118..119].
        FakeUnit sibling = fake(AUnitType.Protoss_Pylon, 87, 118);
        sibling.setCompleted(true);
        // Builder parked on the site: attached and motionless, so resiting
        // disrupts no walk (#53) - same posture as a stuck live builder.
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 86, 118);

        world(30, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            sibling,
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 5 || fired) return;

            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            atlantis.util.log.EventLog.clearForTests();
            tests.fakes.FakeMapTiles.EXPLORED = true;

            // Site (86,118), footprint [86..87]x[118..119]: overlaps the
            // sibling at x=87 but is a different tile - street-grid demand,
            // not a duplicate.
            APosition site = APosition.create(86, 118);
            Construction waiting = new Construction(AUnitType.Protoss_Pylon);
            waiting.setProductionOrder(new ProductionOrder(AUnitType.Protoss_Pylon, 8));
            waiting.setPositionToBuild(site);
            waiting.setBuilder(builder);
            waiting.setLastBuildAttemptNow();
            builder.position = site;
            builder.unitState().setLastPositionChanged(0);
            ConstructionRequests.addConstruction(waiting);

            assertFalse(
                ConstructionStatusChanger.shouldProximityFinish(waiting),
                "sibling overlap resites - only an exact-tile duplicate absorbs");
            assertFalse(
                waiting.status().isTerminal(),
                "resited, not finished: the demand stays queued");
            assertNotEquals(site, waiting.buildPosition(),
                "the site moves off the covered tile instead of being absorbed");
            assertTrue(
                atlantis.util.log.EventLog.toJsonLines().contains("proximity-resited"),
                "the resite is twinned, not silent");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void travellingBuilderKeepsTheOldAbsorb() {
        // Same sibling cover, but the builder is walking in from afar:
        // resiting under a walk splits walker from work (#53), so the
        // old absorb path stands.
        FakeUnit sibling = fake(AUnitType.Protoss_Pylon, 87, 118);
        sibling.setCompleted(true);
        FakeUnit walker = fake(AUnitType.Protoss_Probe, 40, 118);
        walker.lastCommand = "Move";

        world(30, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            sibling,
            walker
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 5 || fired) return;

            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            tests.fakes.FakeMapTiles.EXPLORED = true;

            APosition site = APosition.create(86, 118);
            Construction waiting = new Construction(AUnitType.Protoss_Pylon);
            waiting.setProductionOrder(new ProductionOrder(AUnitType.Protoss_Pylon, 8));
            waiting.setPositionToBuild(site);
            waiting.setBuilder(walker);
            waiting.setLastBuildAttemptNow();
            ConstructionRequests.addConstruction(waiting);

            assertTrue(
                ConstructionStatusChanger.shouldProximityFinish(waiting),
                "travelling builder: the site absorbs as before, no mid-walk move");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }
}
