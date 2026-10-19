package tests.unit;

import atlantis.production.GoalBoardFeeder;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceOrdersFromQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner: once a builder sets out with supply met, it travels and waits -
 * never recalled for minerals. Measured shape: the Core warped first,
 * the walking Pylon builder turned back to minerals, the Assimilator
 * jumped the queue, the Pylon landed last. Delaying a due building for
 * a pseudo-optimization is not an option.
 *
 * <p>Pins the choke point ({@code assignOptimalBuilder}): a live,
 * unstuck builder on a supply-met row is kept across re-assignment
 * triggers with no gather order; below-min-supply rows (and orderless
 * ones) stay fluid.</p>
 */
public class BuilderStaysDispatchedWhenSupplyMetTest extends AbstractTestWithWorld {

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

        final AUnit[] dispatched = new AUnit[]{null};
        final int[] triggerOrders = new int[]{-1};

        world(30, fakeOurs(ours), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                Queue.get().allOrders().list().clear();
                Queue.get().clearCache();
                ConstructionRequests.constructions.clear();
                Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Pylon, 16));
                EventLog.clearForTests();
            }
            else {
                Queue.get().refresh();
            }
            for (ProductionOrder order : new ArrayList<ProductionOrder>(
                Queue.get().readyToProduceOrders().list())) {
                ProduceOrdersFromQueue.handleProductionOrder(order);
            }

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

        assertNotNull(picked[0], "orderless rows still resolve a builder (old path)");
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

    private static List<FakeOrderSink.Record> ordersOf(AUnit builder) {
        return ((FakeOrderSink) ((FakeUnit) builder).orderSink()).orders();
    }
}
