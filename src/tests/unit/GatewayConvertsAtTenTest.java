package tests.unit;

import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.updater.QueueRefresher;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-15: the opening pipeline must make sense to ~15 supply,
 * Gateway at 10 included. Same shape as FirstPylonConvertsAtEightTest:
 * build-order Gateway row (min 10) plus far-future Core/Assimilator HIGH
 * rows, supply 10, healthy bank. Unconvertible HIGH waiters must not
 * block it either.
 */
@Tag("production")
public class GatewayConvertsAtTenTest extends AbstractTestWithWorld {

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

    @Test
    public void gatewayConvertsAtTenDespiteFutureTechRows() {
        currentMinerals = 250;
        currentSupplyUsed = 10;
        currentSupplyTotal = 17;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 22, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 23, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            ProductionOrder core = new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 18);
            ProductionOrder assim = new ProductionOrder(AUnitType.Protoss_Assimilator, 22);
            Queue.get().addNew(0, gateway);
            Queue.get().addNew(1, core);
            Queue.get().addNew(2, assim);

            new QueueRefresher(Queue.get()).refresh();

            assertTrue(
                gateway.isStatus(OrderStatus.READY_TO_PRODUCE),
                "Gateway at 10 with 250 bank must be READY behind unconvertible tech rows");

            assertTrue(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Gateway, gateway),
                "READY Gateway must convert");
            assertEquals(
                1, ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway).size(),
                "exactly one Gateway construction");
            assertNotNull(
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway).get(0).builder(),
                "conversion assigns a builder");
        });
    }
}
