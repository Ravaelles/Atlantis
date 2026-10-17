package tests.acceptance;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.OutstandingBuildings;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.production.orders.production.queue.CountInQueue;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-18: one unfinished building counts once, from every angle
 * that can see it. Queue-only caps went blind on dispatched sites (the
 * supply-12 third Gateway, the double Core); queue+construction sums
 * double-count dispatched rows. Outstanding = undispatched rows +
 * warping units + unfinished constructions (PREPARING included).
 */
public class OutstandingBuildingsCountTest extends WorldStubForTests {

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
    public void queuedPlusWarpingCountsTwo() {
        FakeUnit warping = fake(AUnitType.Protoss_Gateway, 28, 20);
        warping.setCompleted(false);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            warping,
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Gateway, 12));
            Queue.get().clearCache();

            assertEquals(
                2, OutstandingBuildings.ofType(AUnitType.Protoss_Gateway),
                "one queued row + one warping building");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void dispatchedRowCountsOnce() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            ProductionOrder row = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            Queue.get().addNew(0, row);
            Construction site = new Construction(AUnitType.Protoss_Gateway);
            row.setConstruction(site);
            site.setProductionOrder(row);
            ConstructionRequests.constructions.add(site);
            Queue.get().clearCache();

            assertEquals(
                1, OutstandingBuildings.ofType(AUnitType.Protoss_Gateway),
                "a dispatched row lives in constructions - counting both double-counts");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void preparingSiteCounts() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            // PREPARING is the default status of a fresh Construction.
            ConstructionRequests.constructions.add(
                new Construction(AUnitType.Protoss_Gateway));

            assertEquals(
                1, OutstandingBuildings.ofType(AUnitType.Protoss_Gateway),
                "a picked-early site is outstanding even with no builder");
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
