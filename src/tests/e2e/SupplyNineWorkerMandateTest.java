package tests.e2e;

import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-16: with the first Pylon standing at 9/18 and a Gateway
 * row due at 10, the bot built no Probes - it sat to 150 bank for the
 * Gateway instead. Two rules pin the owner's game state:
 *
 * <p>1. No building starts below its supply on a poor bank (the old
 * one-early rule is revoked): the Gateway row waits for supply 10
 * unless the bank covers price+50.</p>
 *
 * <p>2. The worker at supply 9 is mandatory (unless supply-blocked,
 * answered above): the pump flows past the waiter gate. Later gates
 * (CriticalStuff and friends) still apply.</p>
 */
public class SupplyNineWorkerMandateTest extends AbstractTestWithWorld {

    private boolean fired = false;

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

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        fired = false;
    }

    private void ownerGameState(int minerals) {
        currentMinerals = minerals;
        currentGas = 0;
        currentSupplyUsed = 9;
        currentSupplyTotal = 18;
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        ConstructionRequests.constructions.clear();

        ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
        Queue.get().addNew(0, gateway);
        Queue.get().clearCache();
    }

    @Test
    public void poorBankWaitsForSupplyButWorkerFlows() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            ownerGameState(100);

            ProductionOrder gateway = Queue.get().allOrders().list().stream()
                .filter(o -> o.isUnitOrBuilding()
                    && AUnitType.Protoss_Gateway.equals(o.unitType()))
                .findFirst().orElse(null);
            assertTrue(gateway != null, "the Gateway row is queued");

            assertFalse(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Gateway, gateway),
                "supply 9 with 100 bank: no early Gateway start without 150+50");
            assertTrue(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "supply 9, Pylon standing, Gateway due at 10: the worker "
                    + "is mandatory, got: " + AutoProduceWorkersCommander.REASON);
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void richBankBuildsEarly() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            ownerGameState(250);

            ProductionOrder gateway = Queue.get().allOrders().list().stream()
                .filter(o -> o.isUnitOrBuilding()
                    && AUnitType.Protoss_Gateway.equals(o.unitType()))
                .findFirst().orElse(null);
            assertTrue(gateway != null, "the Gateway row is queued");

            assertTrue(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Gateway, gateway),
                "supply 9 with 250 bank: price+50 builds early");
            assertEquals(
                1, ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway).size(),
                "exactly one adoption, no duplicates");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }
}
