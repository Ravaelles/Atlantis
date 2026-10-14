package tests.unit;

import atlantis.production.orders.production.queue.PriorityWaiters;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.production.orders.production.queue.updater.QueueRefresher;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
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
    public void sameTypeRowsFinishExactlyOncePerStandingBuilding() {        world(5, fakeOurs(
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

    @Test
    public void linkedFinishedConstructionWithNoUnitCompletesByExistence() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Assimilator, 24, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            // Game 174710: queue-built Assimilator whose unit the engine
            // never reports (OpenBW buildUnit is always null) - the warp
            // finished, the building stands and mines.
            ProductionOrder assimilator = new ProductionOrder(AUnitType.Protoss_Assimilator, 0);
            Construction done = new Construction(AUnitType.Protoss_Assimilator);
            done.setStatus(ConstructionOrderStatus.FINISHED);
            assimilator.setConstruction(done);
            Queue.get().addNew(0, assimilator);

            new QueueRefresher(Queue.get()).refresh();

            assertTrue(
                assimilator.isStatus(OrderStatus.FINISHED),
                "orphaned Assimilator order must finish against its standing building");
        });
    }

    @Test
    public void linkedActiveConstructionWaitsDespiteStandingSameType() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 21, 21);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            // A warping Nexus-2 tracks a SPECIFIC future building: it must
            // not complete against its standing main, or the reserve drops
            // early and the expansion guard re-queues a third Nexus (#58).
            // The builder ref stays post-warp (Protoss miners return), alive.
            ProductionOrder natural = new ProductionOrder(AUnitType.Protoss_Nexus, 0);
            Construction warping = new Construction(AUnitType.Protoss_Nexus);
            warping.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            warping.setBuilder(builder);
            natural.setConstruction(warping);
            Queue.get().addNew(0, natural);

            new QueueRefresher(Queue.get()).refresh();

            assertFalse(
                natural.isStatus(OrderStatus.FINISHED),
                "an actively warping Nexus-2 must survive its standing main");
        });
    }

    @Test
    public void linkedDeadBuilderCompletesByExistence() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 24, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            // Owner 2026-10-14: a built Gateway still shows as in production
            // at 24 supply. A row whose builder is dead tracks nothing - the
            // standing building satisfies it and it must leave next-orders.
            ProductionOrder row = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            Construction stale = new Construction(AUnitType.Protoss_Gateway);
            stale.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            FakeUnit corpse = fake(AUnitType.Protoss_Probe, 21, 21);
            corpse.setHp(0);
            stale.setBuilder(corpse);
            row.setConstruction(stale);
            Queue.get().addNew(0, row);

            new QueueRefresher(Queue.get()).refresh();

            assertTrue(
                row.isStatus(OrderStatus.FINISHED),
                "IN_PROGRESS row with a dead builder must finish against its standing building");
        });
    }
}
