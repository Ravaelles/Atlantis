package tests.unit;

import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.policy.ProductionRacePolicy;
import atlantis.production.engine.ProductionEngine;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.fakes.FakeOrderSink;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Builder stays dispatched through the v2 engine (owner rule carried over
 * from the legacy drain pin): once a builder sets out with supply met, it
 * travels and waits - never recalled for minerals. A Core warping
 * elsewhere eats the bank mid-walk; the re-assignment keeps the walking
 * builder with no gather order, and the same builder carries the row to
 * BUILDING_STARTED.
 */
@Tag("production")
public class V2BuilderStaysDispatchedTest extends WorldStubForTests {

    private static List<FakeOrderSink.Record> ordersOf(AUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders();
    }

    private static boolean noGatherAfter(AUnit builder, int fromIndex) {
        List<FakeOrderSink.Record> orders = ordersOf(builder);
        for (int i = Math.max(0, fromIndex); i < orders.size(); i++) {
            if (orders.get(i).operation.equals("gather")) return false;
        }
        return true;
    }

    private static boolean startedWithBuilder(int builderId) {
        String log = EventLog.toJsonLines();
        int from = 0;
        while (true) {
            int key = log.indexOf("\"key\":\"BUILDING_STARTED\"", from);
            if (key < 0) return false;

            int end = log.indexOf('\n', key);
            if (end < 0) end = log.length();
            String row = log.substring(key, end);
            if (row.contains("\"type\":\"Pylon\"") && row.contains("\"builder\":" + builderId)) return true;
            from = end;
        }
    }

    @BeforeEach
    public void hermetic() {
        if (Queue.get() != null) Queue.get().clearCache();
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
    }

    @AfterEach
    public void clean() {
        if (Queue.get() != null) Queue.get().clearCache();
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
    }

    @Test
    public void reassignmentKeepsTheWalkingBuilderWhenSupplyMet() {
        currentSupplyUsed = 20;
        currentSupplyTotal = 25;
        currentMinerals = 200;
        FakeUnit[] ours = new FakeUnit[]{
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 24),
            fake(AUnitType.Protoss_Probe, 33, 30),
        };

        atlantis.production.orders.build.ABuildOrder previous =
            atlantis.production.orders.build.CurrentBuildOrder.get();
        atlantis.production.orders.build.ABuildOrder protossBO =
            org.mockito.Mockito.mock(atlantis.production.orders.build.ABuildOrder.class);
        atlantis.production.orders.production.queue.order.ProductionOrder pylonRow =
            new ProductionOrder(AUnitType.Protoss_Pylon, 16);
        org.mockito.Mockito.when(protossBO.productionOrders()).thenReturn(
            new java.util.ArrayList<>(
                java.util.Collections.singletonList(pylonRow)));

        final AUnit[] dispatched = new AUnit[]{null};
        final int[] triggerOrders = new int[]{-1};

        ProductionEngine engine = new ProductionEngine(ProductionRacePolicy.forRace("Protoss"));

        try {
            world(30, fakeOurs(ours), fakeEnemies(), () -> {
                int now = A.now();
                if (now == 1) {
                    atlantis.production.orders.build.CurrentBuildOrder.set(protossBO);
                    ConstructionRequests.constructions.clear();
                    EventLog.clearForTests();
                }

                engine.updateFrame();

                // The Core warps elsewhere and eats the bank mid-walk.
                if (now == 5) currentMinerals = 5;
                if (now == 6 && dispatched[0] == null) {
                    for (Construction construction : new ArrayList<Construction>(
                        ConstructionRequests.notFinished())) {
                        if (construction.builder() != null) {
                            dispatched[0] = construction.builder();
                            // The 96-frame replace check / re-adopt trigger.
                            AUnit kept = construction.assignOptimalBuilder();
                            assertEquals(
                                dispatched[0], kept,
                                "supply met: re-assignment keeps the walking builder");
                            triggerOrders[0] = ordersOf(dispatched[0]).size();
                        }
                    }
                }
                if (now == 10) currentMinerals = 200;
            });
        } finally {
            if (previous != null) atlantis.production.orders.build.CurrentBuildOrder.set(previous);
            else atlantis.production.orders.build.CurrentBuildOrder.clear();
        }

        assertNotNull(dispatched[0], "a builder was dispatched for the Pylon");
        assertTrue(
            noGatherAfter(dispatched[0], triggerOrders[0]),
            "the kept builder was never turned back to minerals: "
                + ordersOf(dispatched[0]));
        assertTrue(
            startedWithBuilder(dispatched[0].id()),
            "the same builder carries the row to BUILDING_STARTED: "
                + EventLog.toJsonLines());
    }

    @Test
    public void orderlessRowsStayFluid() {
        currentSupplyUsed = 20;
        currentSupplyTotal = 25;
        currentMinerals = 200;

        final AUnit[] picked = new AUnit[]{null};
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 24),
            fake(AUnitType.Protoss_Probe, 33, 30)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            picked[0] = construction.assignOptimalBuilder();
        });

        assertNotNull(picked[0], "orderless rows still resolve a builder");
    }
}
