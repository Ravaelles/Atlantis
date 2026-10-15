package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.PrepareConstructionSite;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.updater.QueueRefresher;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-14, extended 2026-10-15: pick the site two supplies before
 * the order unlocks (Pylon at 8 is picked at 6, Gateway at 9 at 7), while
 * still ~100 minerals short of the price. A PREPARING construction holds
 * the reserved site with no builder and no issue; ProduceBuilding adopts
 * it once ready. While poor the site sits marked without costing a worker.
 */
public class PreparingConstructionSiteTest extends AbstractTestWithWorld {

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
    public void sitePickedOneSupplyEarlyWithNoBuilder() {
        currentMinerals = 100;
        currentSupplyUsed = 7;
        currentSupplyTotal = 17;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Pylon, 8));

            new QueueRefresher(Queue.get()).refresh();

            assertEquals(
                1, ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).size(),
                "one supply early the Pylon site must be picked");
            Construction prepared =
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).get(0);
            assertEquals(ConstructionOrderStatus.PREPARING, prepared.status());
            assertNotNull(prepared.buildPosition(), "site must be marked");
            assertNull(prepared.builder(), "no builder assigned while preparing");
        });
    }

    @Test
    public void readyOrderAdoptsPreparedSite() {
        currentMinerals = 400;
        currentSupplyUsed = 9;
        currentSupplyTotal = 17;

        FakeUnit builder = fake(AUnitType.Protoss_Probe, 21, 21);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Queue.get().addNew(0, order);
            new QueueRefresher(Queue.get()).refresh();

            Construction prepared =
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).get(0);
            prepared.setBuilder(builder);
            order.setStatus(OrderStatus.READY_TO_PRODUCE);

            assertTrue(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Pylon, order),
                "ready order must adopt the prepared site");
            assertEquals(
                ConstructionOrderStatus.NOT_STARTED, prepared.status(),
                "adoption starts the construction, no new search");
            assertEquals(
                1, ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).size(),
                "no duplicate construction on adopt");
        });
    }

    @Test
    public void adoptWaitsForFreeBuilder() {
        currentMinerals = 400;
        currentSupplyUsed = 9;
        currentSupplyTotal = 17;

        FakeUnit corpse = fake(AUnitType.Protoss_Probe, 21, 21);
        corpse.setHp(0);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            corpse
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Queue.get().addNew(0, order);
            new QueueRefresher(Queue.get()).refresh();

            Construction prepared =
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).get(0);
            order.setStatus(OrderStatus.READY_TO_PRODUCE);

            // The stub offers no free builder here (GetOptimalBuilder finds
            // none) - adoption must wait, not duplicate or drop the site.
            assertFalse(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Pylon, order),
                "no free builder: stay PREPARING, retry next frames");
            assertEquals(
                ConstructionOrderStatus.PREPARING, prepared.status());
            assertEquals(
                1, ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).size(),
                "no duplicate construction while waiting for a builder");
        });
    }

    @Test
    public void preparingNeverGoesOverdue() {
        currentMinerals = 600;
        currentSupplyUsed = 20;
        currentSupplyTotal = 40;

        world(700, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 5) {
                Construction preparing = new Construction(AUnitType.Protoss_Pylon);
                preparing.setStatus(ConstructionOrderStatus.PREPARING);
                ConstructionRequests.addConstruction(preparing);

                Construction stale = new Construction(AUnitType.Protoss_Pylon);
                ConstructionRequests.addConstruction(stale);
            }
            if (atlantis.game.A.now() != 690) return;

            for (Construction construction : ConstructionRequests.all()) {
                if (construction.status() == ConstructionOrderStatus.PREPARING) {
                    assertFalse(
                        construction.isOverdue(),
                        "a 685-frame-old PREPARING site waits by design, never overdue");
                }
                if (construction.status() == ConstructionOrderStatus.NOT_STARTED
                    && construction.builder() == null) {
                    assertTrue(
                        construction.isOverdue(),
                        "control: builderless NOT_STARTED of the same age is overdue");
                }
            }
        });
    }

    @Test
    public void cancelTooLongSkipsPreparing() {
        currentMinerals = 600;
        currentSupplyUsed = 20;
        currentSupplyTotal = 40;

        world(700, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 5) {
                ConstructionRequests.constructions.clear();
                Construction preparing = new Construction(AUnitType.Protoss_Pylon);
                preparing.setStatus(ConstructionOrderStatus.PREPARING);
                ConstructionRequests.addConstruction(preparing);
            }
            // Game 200018: the cancel-too-long path killed a 48 s old
            // PREPARING site and the vetoed requeue deadlocked 9/9. A site
            // that was never worked on cannot take too long.
            if (atlantis.game.A.now() != 690) return;

            Construction preparing = ConstructionRequests.all().stream()
                .filter(c -> c.status() == ConstructionOrderStatus.PREPARING)
                .findFirst().orElse(null);
            assertNotNull(preparing, "sanity: the PREPARING site is still listed");
            atlantis.production.constructions.commanders.CancelTooLongConstructions
                .cancelCauseTakingTooLongIfNeeded(
                    preparing, AUnitType.Protoss_Pylon, AUnitType.Protoss_Pylon);

            assertTrue(
                ConstructionRequests.constructions.contains(preparing),
                "cancel-too-long must never fire on a PREPARING site");
        });
    }

    @Test
    public void danglingOrderLinksAreCleared() {
        currentMinerals = 100;
        currentSupplyUsed = 7;
        currentSupplyTotal = 17;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            // An order linked to a construction that is no longer listed
            // (cancelled elsewhere without unlinking) skips both the
            // preparer (linked) and the handler (stale) forever.
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Queue.get().addNew(0, order);
            Construction ghost = new Construction(AUnitType.Protoss_Pylon);
            order.setConstruction(ghost);
            Queue.get().clearCache();

            PrepareConstructionSite.dropOrphanedPreparing();

            assertNull(
                order.construction(),
                "dangling link must clear so the order can prepare/convert again");
        });
    }

    @Test
    public void gatewaySitePickedTwoSuppliesEarlyWhilePoor() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            currentMinerals = 40;
            currentSupplyTotal = 17;
            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Gateway, 9));

            currentSupplyUsed = 6;
            new QueueRefresher(Queue.get()).refresh();
            assertTrue(
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway).isEmpty(),
                "at 6 for At 9 nothing is picked yet - two supplies early, not three");

            currentSupplyUsed = 7;
            Queue.get().clearCache();
            new QueueRefresher(Queue.get()).refresh();
            assertEquals(
                1, ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway).size(),
                "at 7 for At 9 the site must be picked while still ~100 short of the price");
            Construction prepared =
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway).get(0);
            assertEquals(ConstructionOrderStatus.PREPARING, prepared.status());
            assertNotNull(prepared.buildPosition(), "site must be marked");
            assertNull(prepared.builder(), "no builder assigned while preparing");
        });
    }
}
