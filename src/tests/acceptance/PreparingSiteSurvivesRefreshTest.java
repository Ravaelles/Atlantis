package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.builders.RefreshConstructionPosition;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-18: a picked-early site stays put. Refresh exists for a
 * traveling builder whose target went bad - with no builder dispatched
 * yet there is nothing to save, only churn to make. Adoption
 * re-validates at issue time anyway.
 */
public class PreparingSiteSurvivesRefreshTest extends WorldStubForTests {

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
    public void preparingSiteIsNotMoved() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;

            APosition site = APosition.create(16, 46);
            Construction preparing = new Construction(AUnitType.Protoss_Pylon);
            preparing.setPositionToBuild(site);
            preparing.setStatus(ConstructionOrderStatus.PREPARING);

            assertSame(
                site, RefreshConstructionPosition.refreshIfNeeded(preparing),
                "a PREPARING site returns untouched, whatever the map says");
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
