package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.NewConstructionRequest;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.workers.FreeWorkers;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A scout picked as builder never builds (it walks off exploring) and a
 * builder picked as scout never scouts - both directions are closed.
 * Builder is an assignment, not a moment: linked-but-waiting Protoss
 * probes count as builders even though they construct nothing yet.
 */
@Tag("production")
public class BuilderPickSkipsScoutsTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void hermetic() {
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
    }

    @AfterEach
    public void clean() {
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void loneScoutIsNotBurnedAsBuilder() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe(21, 24, true)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            currentSupplyUsed = 20;

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Construction construction =
                NewConstructionRequest.requestConstructionOf(AUnitType.Protoss_Pylon, null, order);

            assertTrue(
                construction == null || construction.builder() == null
                    || !construction.builder().isScout(),
                "no spare worker: wait for one, never burn the scout");
        });
    }

    @Test
    public void freeWorkerPreferredOverScout() {
        FakeUnit scout = probe(21, 24, true);
        FakeUnit worker = probe(22, 24, false);

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            scout, worker
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            currentSupplyUsed = 20;

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Construction construction =
                NewConstructionRequest.requestConstructionOf(AUnitType.Protoss_Pylon, null, order);

            assertNotNull(construction, "a free worker exists, the request stands");
            assertNotNull(construction.builder(), "someone is assigned");
            assertFalse(
                construction.builder().isScout(),
                "the free worker builds, the scout keeps scouting");
            assertEquals(worker, construction.builder(), "nearest free, not first in list");
        });
    }

    @Test
    public void notBuilderDropsOnlyLinkedWorkers() {
        FakeUnit linked = probe(21, 24, false);
        FakeUnit free = probe(22, 24, false);

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            linked, free
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setBuilder(linked);
            construction.setPositionToBuild(
                atlantis.map.position.APosition.create(95, 72));
            ConstructionRequests.addConstruction(construction);

            assertTrue(
                atlantis.units.select.Select.ourWorkers().notBuilder().list().contains(free),
                "free workers stay eligible");
            assertFalse(
                atlantis.units.select.Select.ourWorkers().notBuilder().list().contains(linked),
                "an assigned waiter is a builder even while it constructs nothing");
            assertFalse(
                FreeWorkers.get().list().contains(linked),
                "the free-worker pool never serves assigned builders");
        });
    }

    private static FakeUnit probe(int tx, int ty, boolean scout) {
        FakeUnit unit = fake(AUnitType.Protoss_Probe, tx, ty);
        unit.setScout(scout);
        return unit;
    }
}
