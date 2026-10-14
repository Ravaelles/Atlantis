package tests.unit;

import atlantis.production.dynamic.protoss.buildings.ProduceFirstAssimilator;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Game 174710: two concurrent Assimilator waiters for one geyser - the
 * build-order row (supply 22) sat outside the next-2 cap the dynamic path
 * checks, so it added a second at 4:04. A progressing row blocks the
 * dynamic add (a stuck notStarted one is still replaced by it).
 */
public class FirstAssimilatorNoDuplicateTest extends AbstractTestWithWorld {

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
    public void progressingRowBlocksDynamicAdd() {
        currentMinerals = 400;
        currentGas = 0;
        currentSupplyUsed = 50;
        currentSupplyTotal = 60;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Cybernetics_Core, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            // Fill the next-2 window with other orders so the cap misses the
            // READY Assimilator row exactly like game 174710.
            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Pylon, 16));
            Queue.get().addNew(1, new ProductionOrder(AUnitType.Protoss_Gateway, 18));
            ProductionOrder row = new ProductionOrder(AUnitType.Protoss_Assimilator, 22);
            row.setStatus(OrderStatus.IN_PROGRESS);
            Queue.get().addNew(2, row);
            Queue.get().clearCache();

            assertFalse(
                ProduceFirstAssimilator.produce(),
                "a progressing Assimilator row must block the dynamic add (one geyser)");
            assertEquals("AssimilatorInProgress", ProduceFirstAssimilator.reason);
        });
    }
}
