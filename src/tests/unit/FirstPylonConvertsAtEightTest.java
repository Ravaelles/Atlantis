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
 * Owner 2026-10-14 (Wine): the first Pylon must convert at supply 8 with
 * ~100 bank - not sit PREPARING until 350. The whole pipeline in one test:
 * build-order Pylon row (min 8) plus far-future Core/Assimilator HIGH rows
 * (min 18/22, unconvertible), supply 8/9, bank 150. Unconvertible HIGH
 * waiters must not block a READY Pylon. Supply is 8/9 (pre-Pylon, no
 * standing Pylon), which is also below the Core-outranks-Pylons floor -
 * the first Pylon is never subject to that rule by construction.
 */
@Tag("production")
public class FirstPylonConvertsAtEightTest extends AbstractTestWithWorld {

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
    public void pylonConvertsAtEightDespiteFutureTechRows() {
        currentMinerals = 150;
        currentSupplyUsed = 8;
        currentSupplyTotal = 9;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            ProductionOrder pylon = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            ProductionOrder core = new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 18);
            ProductionOrder assim = new ProductionOrder(AUnitType.Protoss_Assimilator, 22);
            Queue.get().addNew(0, pylon);
            Queue.get().addNew(1, core);
            Queue.get().addNew(2, assim);

            new QueueRefresher(Queue.get()).refresh();

            assertTrue(
                pylon.isStatus(OrderStatus.READY_TO_PRODUCE),
                "Pylon at 8 with 150 bank must be READY behind unconvertible tech rows");

            assertTrue(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Pylon, pylon),
                "READY Pylon must convert (adopt prepared site or fresh request)");
            assertEquals(
                1, ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).size(),
                "exactly one Pylon construction");
            assertNotNull(
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).get(0).builder(),
                "conversion assigns a builder");
        });
    }
}
