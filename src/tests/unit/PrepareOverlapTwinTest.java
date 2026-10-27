package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.PrepareConstructionSite;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The pick-time overlap unlink was the last silent exit in the chain:
 * a site the spiral allowed but a listed sibling covers unlinked with
 * no row (measured live: FE run `112337` minted 40 construction ids
 * for 1 logged PREPARING while the Forge never started; the twin-less
 * unlink is the only branch consistent with the burn). The adopt side
 * already names its mirror (`adopt-site-blocked`); the prepare side
 * names its own.
 */
@Tag("infra")
public class PrepareOverlapTwinTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void clearLog() {
        EventLog.clearForTests();
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
    }

    @Test
    public void overlappingPickUnlinksLoudly() {
        currentMinerals = 400;
        currentSupplyUsed = 9;
        currentSupplyTotal = 17;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            // A listed sibling PREPARING (no unit yet, so the exact-tile
            // occupancy check passes but the footprints intersect).
            ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            Construction sibling = new Construction(AUnitType.Protoss_Gateway);
            sibling.setPositionToBuild(APosition.create(100, 100));
            sibling.setStatus(ConstructionOrderStatus.PREPARING);
            sibling.setProductionOrder(gateway);
            ConstructionRequests.addConstruction(sibling);

            // Pylon exact tile shares the (102-103, 101-102) corner with
            // the Gateway footprint: the pick is allowed by the spiral
            // but must die in the pick-time guard - loudly.
            ProductionOrder pylon = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            pylon.setAroundPosition(APosition.create(102, 101));
            pylon.markAsUsingExactPosition();

            EventLog.clearForTests();
            PrepareConstructionSite.prepareIfDue(pylon);

            assertNull(
                pylon.construction(),
                "an overlapping pick must unlink, got: " + pylon.construction());
            assertTrue(
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).isEmpty(),
                "an overlapping pick must not list a construction");
            assertTrue(
                EventLog.toJsonLines().contains("prepare-site-overlap"),
                "the unlink must name its guard, got: " + EventLog.toJsonLines());
        });
    }
}
