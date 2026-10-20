package tests.unit;

import atlantis.production.orders.production.queue.PriorityWaiters;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Priority waiters total the price of unfinished, non-ignored
 * building/upgrade orders strictly above the spender's level (D3).
 *
 * <p>Runs inside a stub world because the queue singleton only exists after
 * game start; live commanders fill it during setup frames, so the queue is
 * cleared first for determinism (same pattern as
 * ExpansionNoSecondBaseWhileOnePendingTest).</p>
 */
@Tag("infra")
public class PriorityWaitersTest extends AbstractTestWithWorld {

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
    public void waitersTotalByPriority() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().history().clear();
            Queue.get().clearCache();
            assertEquals(0, PriorityWaiters.mineralsAbove(ProductionOrderPriority.STANDARD));

            ProductionOrder nexus = new ProductionOrder(AUnitType.Protoss_Nexus, 0);
            nexus.setPriority(ProductionOrderPriority.HIGH);
            Queue.get().addNew(0, nexus);
            Queue.get().clearCache();
            assertEquals(400, PriorityWaiters.mineralsAbove(ProductionOrderPriority.STANDARD));
            assertEquals(0, PriorityWaiters.gasAbove(ProductionOrderPriority.STANDARD));
            assertEquals(0, PriorityWaiters.mineralsAbove(ProductionOrderPriority.HIGH));

            ProductionOrder pylon = new ProductionOrder(AUnitType.Protoss_Pylon, 0);
            Queue.get().addNew(1, pylon);
            Queue.get().clearCache();
            assertEquals(
                400, PriorityWaiters.mineralsAbove(ProductionOrderPriority.STANDARD),
                "a STANDARD pylon waits for nothing itself");

            nexus.setStatus(OrderStatus.FINISHED);
            Queue.get().clearCache();
            assertEquals(
                0, PriorityWaiters.mineralsAbove(ProductionOrderPriority.STANDARD),
                "a finished waiter stops blocking");
        });
    }

    @Test
    public void supplyProvidersNeverBlock() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().history().clear();
            Queue.get().clearCache();

            ProductionOrder pylon = new ProductionOrder(AUnitType.Protoss_Pylon, 0);
            pylon.setPriority(ProductionOrderPriority.TOP);
            Queue.get().addNew(0, pylon);
            Queue.get().clearCache();

            assertEquals(
                0, PriorityWaiters.mineralsAbove(ProductionOrderPriority.STANDARD),
                "routine supply urgency must not freeze tech (game 152437)");
        });
    }

    @Test
    public void techBuildingsDefaultHigh() {
        assertEquals(
            ProductionOrderPriority.HIGH,
            new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 0).priority());
        assertEquals(
            ProductionOrderPriority.HIGH,
            new ProductionOrder(AUnitType.Protoss_Assimilator, 0).priority());
        assertEquals(
            ProductionOrderPriority.STANDARD,
            new ProductionOrder(AUnitType.Protoss_Gateway, 0).priority());
        assertEquals(
            ProductionOrderPriority.STANDARD,
            new ProductionOrder(AUnitType.Protoss_Pylon, 0).priority());
    }
}
