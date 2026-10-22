package tests.unit;

import atlantis.production.GoalBoardFeeder;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.PrepareConstructionSite;
import atlantis.information.enemy.UnitsArchive;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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
 * Silent-death half of the fresh-request path (measured Wine 7th_rav:
 * Gateway At-10 requested a site, the order was consumed on the
 * request, and the NOT_STARTED construction sat builderless with no
 * advance path - handler skips linked non-PREPARING rows, refresh
 * skips consumed ones, no twin fires). A NOT_STARTED site with no
 * live builder and a live order re-enters the normal path visibly.
 */
@Tag("production")
public class BuilderlessNotStartedRetriesTest extends AbstractTestWithWorld {

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
        GoalBoardFeeder.clearForTests();
        EventLog.clearForTests();
        UnitsArchive.reset();
    }

    @AfterEach
    public void clean() {
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
        GoalBoardFeeder.clearForTests();
        UnitsArchive.reset();
    }

    @Test
    public void builderlessNotStartedUnlinksAndRetriesVisibly() {
        currentSupplyUsed = 20;
        currentSupplyTotal = 25;
        currentMinerals = 300;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 24)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Queue.get().addNew(0, order);
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setProductionOrder(order);
            order.setConstruction(construction);
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            construction.setPositionToBuild(
                atlantis.map.position.APosition.create(95, 72));
            ConstructionRequests.addConstruction(construction);
            order.consume();
            EventLog.clearForTests();

            PrepareConstructionSite.dropOrphanedPreparing();

            assertFalse(
                ConstructionRequests.constructions.contains(construction),
                "dead site leaves the list instead of rotting");
            assertTrue(
                order.construction() == null
                    || order.construction() != construction,
                "the row unlinks so the normal path can re-drive it");
            assertTrue(
                order.isStatus(OrderStatus.NOT_READY),
                "pristine row state, ready to be evaluated again");
            assertTrue(
                EventLog.toJsonLines().contains("builderless-retry"),
                "the retry names itself instead of rotting silently: "
                    + EventLog.toJsonLines());
        });
    }

    @Test
    public void deadBuilderSameAsNoBuilder() {
        currentSupplyUsed = 20;
        currentSupplyTotal = 25;
        currentMinerals = 300;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 24)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            FakeUnit probe = fake(AUnitType.Protoss_Probe, 22, 24);
            probe.setHp(0);
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Queue.get().addNew(0, order);
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setProductionOrder(order);
            order.setConstruction(construction);
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            construction.setBuilder(probe);
            construction.setPositionToBuild(
                atlantis.map.position.APosition.create(95, 72));
            ConstructionRequests.addConstruction(construction);
            order.consume();
            EventLog.clearForTests();

            PrepareConstructionSite.dropOrphanedPreparing();

            assertEquals(
                1, countRows("builder-dead-retry"),
                "a dead builder reads the same as none: "
                    + EventLog.toJsonLines());
        });
    }

    @Test
    public void archivedDeadBuilderRetriesLikeNoBuilder() {
        currentSupplyUsed = 20;
        currentSupplyTotal = 25;
        currentMinerals = 300;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 24)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            // Builder died (UNIT_DIED twin fired, archive knows) but the
            // wrapper still reports hp > 0 - the live cid-331 shape
            // (Wine macro 2026-10-21). The retry path must read the
            // archive, not the stale hp, or the site rots silently.
            FakeUnit probe = fake(AUnitType.Protoss_Probe, 22, 24);
            UnitsArchive.markUnitAsDestroyed(probe);
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Queue.get().addNew(0, order);
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setProductionOrder(order);
            order.setConstruction(construction);
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            construction.setBuilder(probe);
            construction.setPositionToBuild(
                atlantis.map.position.APosition.create(95, 72));
            ConstructionRequests.addConstruction(construction);
            order.consume();
            EventLog.clearForTests();

            PrepareConstructionSite.dropOrphanedPreparing();

            assertTrue(
                probe.isAlive(),
                "fixture check: stale hp still reads alive (else this"
                    + " pins the wrong shape)");
            assertEquals(
                1, countRows("builder-dead-retry"),
                "an archived-dead builder retries visibly instead of"
                    + " rotting silently: "
                    + EventLog.toJsonLines());
        });
    }

    private static int countRows(String reason) {
        String log = EventLog.toJsonLines();
        int count = 0;
        int from = 0;
        while (true) {
            int at = log.indexOf(reason, from);
            if (at < 0) return count;

            count++;
            from = at + 1;
        }
    }
}
