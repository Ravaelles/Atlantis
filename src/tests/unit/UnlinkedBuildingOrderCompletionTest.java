package tests.unit;

import atlantis.production.orders.production.queue.PriorityWaiters;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.production.orders.production.queue.updater.QueueRefresher;
import atlantis.units.AUnitType;
import bwapi.Race;
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
 * Game 172954: a queued Gateway-2 waited 177x behind HIGH Core/Assimilator
 * waiters while BOTH buildings stood. Those build-order rows were built by
 * the legacy path without a linked Construction, and IsOrderCompleted
 * answers false forever on construction()==null - phantom waiters that
 * never converge. An unlinked order is done when its building stands.
 *
 * <p>Same-type rows are fungible but counted: two Gateway rows with one
 * standing Gateway finish exactly one.</p>
 */
public class UnlinkedBuildingOrderCompletionTest extends AbstractTestWithWorld {

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
    public void unlinkedOrderCompletesWhenBuildingStands() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Cybernetics_Core, 24, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            ProductionOrder core = new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 0);
            Queue.get().addNew(0, core);
            Queue.get().clearCache();
            assertEquals(
                200, PriorityWaiters.mineralsAbove(ProductionOrderPriority.STANDARD),
                "sanity: the Core waiter blocks before refresh");

            new QueueRefresher(Queue.get()).refresh();

            assertTrue(
                core.isStatus(OrderStatus.FINISHED),
                "standing completed Core must finish its unlinked order (game 172954)");
            Queue.get().clearCache();
            assertEquals(
                0, PriorityWaiters.mineralsAbove(ProductionOrderPriority.STANDARD),
                "no phantom waiter may survive its building");
        });
    }

    @Test
    public void sameTypeRowsFinishExactlyOncePerStandingBuilding() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 24, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            ProductionOrder first = new ProductionOrder(AUnitType.Protoss_Gateway, 0);
            ProductionOrder second = new ProductionOrder(AUnitType.Protoss_Gateway, 0);
            Queue.get().addNew(0, first);
            Queue.get().addNew(1, second);

            new QueueRefresher(Queue.get()).refresh();

            assertTrue(first.isStatus(OrderStatus.FINISHED), "one standing Gateway finishes one row");
            assertFalse(
                second.isStatus(OrderStatus.FINISHED),
                "the second row still wants a second Gateway built");
        });
    }
}
