package tests.acceptance;

import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.PrepareConstructionSite;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-16, MANDATORY: while a Pylon is already warping, no new
 * Pylon site may enter PREPARING - the row waits as a plain queued
 * order until the warping one completes. A site picked minutes early
 * for a building that is coming anyway only churns. PENDING, never
 * PREPARING.
 */
public class NoPreparingWhileWarpingTest extends WorldStubForTests {

    private boolean fired = false;

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
    public void warpingPylonBlocksPreparing() {
        FakeUnit warping = fake(AUnitType.Protoss_Pylon, 30, 30);
        warping.setCompleted(false);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            warping,
            fake(AUnitType.Protoss_Probe, 11, 11)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentSupplyUsed = 7;
            currentSupplyTotal = 17;

            ProductionOrder pylon = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Queue.get().addNew(0, pylon);
            Queue.get().clearCache();

            PrepareConstructionSite.prepareIfDue(pylon);

            assertTrue(
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).isEmpty(),
                "a warping Pylon is coming - no second site may enter PREPARING");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void noWarpingPylonPreparesNormally() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Pylon, 30, 30),
            fake(AUnitType.Protoss_Probe, 11, 11)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentSupplyUsed = 7;
            currentSupplyTotal = 17;

            ProductionOrder pylon = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Queue.get().addNew(0, pylon);
            Queue.get().clearCache();

            PrepareConstructionSite.prepareIfDue(pylon);

            assertFalse(
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).isEmpty(),
                "a standing (completed) Pylon is no reason to wait - prepare");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
