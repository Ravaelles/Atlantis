package tests.unit;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.NewConstructionRequest;
import atlantis.production.constructions.position.FindPosition;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitProductionCommander;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitProductionCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A search that fails on a blocked base must WAIT, not die (NEXT #192):
 * the Zealot Hell flood ran on 2 Gateways because 9 READY Gateway orders
 * were minted and instantly cancelled one by one ("Null position for
 * Gateway") while the army stood on every tile; zero warps after f4486.
 * A blocked search window is transient clutter, not a stuck loop - the
 * order survives and retries, and only a failure that outlasts the
 * patience still cancels (the stuck-loop guard stays, delayed).
 *
 * <p>The twin carries the search window (around + maxDist): the live
 * choice between "transient clutter" and "wrong search center" is
 * unreadable without them.
 *
 * <p>Shape note: the test drives {@code FindPosition.findForBuilding}
 * directly, like the other search tests - the upper entry
 * ({@code APositionFinder.findPositionForNew}) answers a random tile
 * in tests, so the full request path cannot fail there by construction.
 */
@Tag("production")
public class GatewayWaitsOnBlockedBaseTest extends WorldStubForTests {

    private boolean fired = false;
    private List<FakeUnit> blockers = new ArrayList<>();
    private FakeUnit probe;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void clearLog() {
        EventLog.clearForTests();
        ReservedResources.reset();
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
        ReservedResources.reset();
    }

    @Test
    public void blockedBaseKeepsTheOrderAlive() {
        worldWithBlockedBase(() -> {
            ProductionOrder gateway = gatewayOrder();
            EventLog.clearForTests();

            for (int i = 0; i < 100; i++) {
                assertNull(
                    searchFor(gateway),
                    "the blocked search finds nothing (attempt " + i + ")");
                NewConstructionRequest.handleFailedSearch(
                    AUnitType.Protoss_Gateway, gateway, dominant());
            }

            assertTrue(
                queueContains(gateway),
                "a blocked-base order waits, it is not cancelled after 100 failed searches");
            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"PLACEMENT_FAILED\""),
                "the failed search must be twinned, got: " + rows);
            assertTrue(rows.contains("\"around\":\"[32,30]\""),
                "the twin must carry the search center, got: " + rows);
            assertTrue(rows.contains("\"maxDist\":3"),
                "the twin must carry the search radius, got: " + rows);
        });
    }

    @Test
    public void stuckOrderStillDiesAfterPatience() {
        worldWithBlockedBase(() -> {
            ProductionOrder gateway = gatewayOrder();
            String dominant = dominant();

            for (int i = 0; i < 480; i++) {
                NewConstructionRequest.handleFailedSearch(
                    AUnitType.Protoss_Gateway, gateway, dominant);
            }
            assertTrue(
                queueContains(gateway),
                "patience is not over yet after 480 failed searches");

            for (int i = 0; i < 5; i++) {
                NewConstructionRequest.handleFailedSearch(
                    AUnitType.Protoss_Gateway, gateway, dominant);
            }
            assertTrue(
                !queueContains(gateway),
                "a failure that outlasts the patience still cancels (stuck-loop guard)");
        });
    }

    @Test
    public void freedBaseWarps() {
        ProductionOrder[] held = new ProductionOrder[1];
        worldWithBlockedBase(() -> {
            held[0] = gatewayOrder();

            assertNull(searchFor(held[0]),
                "the blocked search finds nothing, dominant: " + dominant());
        }, () -> {
            for (FakeUnit blocker : blockers) {
                blocker.position = APosition.create(100 + (blocker.id() % 20), 100);
            }

            assertNotNull(searchFor(held[0]),
                "a freed tile is found on the waiting order, dominant: " + dominant());
            assertTrue(queueContains(held[0]), "the warping order is not cancelled");
        });
    }

    @Test
    public void waitingOrdersDoNotStarveUnitProduction() {
        currentMinerals = 410;
        currentGas = 250;
        currentSupplyUsed = 30;
        currentSupplyTotal = 40;

        List<FakeUnit> ours = new ArrayList<>();
        ours.add(fake(AUnitType.Protoss_Nexus, 26, 30));
        ours.add(fake(AUnitType.Protoss_Pylon, 32, 30));
        ours.add(fake(AUnitType.Protoss_Cybernetics_Core, 26, 26));
        ours.add(fake(AUnitType.Protoss_Gateway, 22, 28));
        ours.add(fake(AUnitType.Protoss_Gateway, 23, 28));
        ours.add(fake(AUnitType.Protoss_Probe, 26, 32));
        ours.add(fake(AUnitType.Protoss_Dragoon, 24, 26));
        ours.add(fake(AUnitType.Protoss_Dragoon, 25, 26));
        ours.add(fake(AUnitType.Protoss_Dragoon, 24, 27));
        ours.add(fake(AUnitType.Protoss_Dragoon, 25, 27));
        List<FakeUnit> blockers = new ArrayList<>();
        for (int tx = 29; tx <= 35; tx++) {
            for (int ty = 27; ty <= 33; ty++) {
                blockers.add(fake(AUnitType.Protoss_Zealot, tx, ty));
            }
        }
        ours.addAll(blockers);

        final int[] tick = { 0 };
        final int[] trainedBefore = { -1 };
        world(21, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(), () -> {
            tick[0]++;
            // Keep the null-position guard live every frame (its clock
            // reads seconds, the world clock reads frames).
            A.setNow(2100 + tick[0], 71);

            if (tick[0] == 1) {
                for (int i = 0; i < 2; i++) {
                    ProductionOrder gateway = new ProductionOrder(
                        AUnitType.Protoss_Gateway, APosition.create(32, 30), 12);
                    gateway.setMaximumDistance(3);
                    Queue.get().addNew(0, gateway);
                    gateway.setStatus(OrderStatus.READY_TO_PRODUCE);
                }
                assertEquals(300, ReservedResources.minerals(),
                    "two READY gateways reserve their price up front");
                trainedBefore[0] = countTrainedDragoons();
            }
            if (tick[0] >= 1 && tick[0] <= 6) {
                for (ProductionOrder order : Queue.get().allOrders()) {
                    if (order.unitType() == AUnitType.Protoss_Gateway) {
                        assertNull(searchFor(order),
                            "the blocked search finds nothing");
                        NewConstructionRequest.handleFailedSearch(
                            AUnitType.Protoss_Gateway, order, dominant());
                    }
                }
            }
            if (tick[0] == 7) {
                assertEquals(0, ReservedResources.minerals(),
                    "failed searches hold no minerals while waiting - the earmark belongs to committed spending");
            }
            if (tick[0] >= 7) {
                new ProtossDynamicUnitProductionCommander().forceHandle();
            }
            if (tick[0] == 9) {
                assertTrue(countTrainedDragoons() > trainedBefore[0],
                    "an affordable Dragoon trains despite the blocked base, commander says: "
                        + ProtossDynamicUnitProductionCommander.reason);
            }
        });
    }

    private int countTrainedDragoons() {
        int count = 0;
        for (AUnitType type : tests.fakes.FakeUnitData.TRAIN) {
            if (type == AUnitType.Protoss_Dragoon) count++;
        }

        return count;
    }

    private APosition searchFor(ProductionOrder order) {
        Construction construction = new Construction(AUnitType.Protoss_Gateway);
        construction.setProductionOrder(order);
        construction.setNearTo(order.aroundPosition());
        construction.setMaxDistance(order.maximumDistance());

        return FindPosition.findForBuilding(
            probe, AUnitType.Protoss_Gateway, construction,
            order.aroundPosition(), order.maximumDistance());
    }

    private String dominant() {
        return NewConstructionRequest.dominantRefusal(AUnitType.Protoss_Gateway);
    }

    private void worldWithBlockedBase(Runnable frame5) {
        worldWithBlockedBase(frame5, () -> {
        });
    }

    private void worldWithBlockedBase(Runnable frame5, Runnable frame6) {
        currentMinerals = 400;
        currentSupplyUsed = 12;
        currentSupplyTotal = 17;

        List<FakeUnit> ours = new ArrayList<>();
        ours.add(fake(AUnitType.Protoss_Nexus, 26, 30));
        ours.add(fake(AUnitType.Protoss_Pylon, 32, 30));
        probe = fake(AUnitType.Protoss_Probe, 26, 32);
        ours.add(probe);

        // Every integer tile the radius-3 spiral can examine carries a
        // zealot: the search window is fully occupied, power is fine.
        blockers = new ArrayList<>();
        for (int tx = 29; tx <= 35; tx++) {
            for (int ty = 27; ty <= 33; ty++) {
                blockers.add(fake(AUnitType.Protoss_Zealot, tx, ty));
            }
        }
        ours.addAll(blockers);

        world(10, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(), () -> {
            if ((A.now() != 5 && A.now() != 6) || fired) return;

            if (A.now() == 5) {
                fired = true;

                // Late game: the null-position guard is live (A.s >= 70, supply >= 11).
                A.setNow(2100, 71);
                frame5.run();
            }
            else {
                // Fresh frame, fresh Select caches: the teleported blockers are gone.
                frame6.run();
            }
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    private ProductionOrder gatewayOrder() {
        ProductionOrder gateway = new ProductionOrder(
            AUnitType.Protoss_Gateway, APosition.create(32, 30), 12);
        gateway.setMaximumDistance(3);
        Queue.get().addNew(0, gateway);

        return gateway;
    }

    private boolean queueContains(ProductionOrder order) {
        for (ProductionOrder queued : Queue.get().allOrders()) {
            if (queued.id() == order.id()) return true;
        }

        return false;
    }
}
