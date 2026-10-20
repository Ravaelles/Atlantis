package tests.unit;

import atlantis.production.GoalBoardFeeder;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceOrdersFromQueue;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Early builder dispatch (owner): with supply met, the builder is
 * assigned once the bank is within one walk of the price - not at
 * zero (reckless), not only at full price plus hoard (late). Drip is
 * +8/frame, so the BUILDING_STARTED twin must carry a bank in
 * [price-50, price+8): the golden middle between a 30-frame idle
 * builder and a ~15 s late start.
 */
@Tag("production")
public class EarlyBuilderDispatchTest extends AbstractTestWithWorld {

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
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        ConstructionRequests.constructions.clear();
        GoalBoardFeeder.clearForTests();
        EventLog.clearForTests();
    }

    @AfterEach
    public void clean() {
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
        GoalBoardFeeder.clearForTests();
    }

    @Test
    public void builderDispatchedWithinHeadroomNotAtFullBank() {
        currentSupplyUsed = 16;
        currentSupplyTotal = 17;
        currentMinerals = 0;
        FakeUnit[] ours = new FakeUnit[16];
        ours[0] = fake(AUnitType.Protoss_Nexus, 20, 20);
        for (int i = 0; i < 14; i++) ours[1 + i] = fake(AUnitType.Protoss_Probe, 21 + i, 24);
        ours[15] = fake(AUnitType.Protoss_Zealot, 40, 21);

        world(60, fakeOurs(ours), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            currentMinerals += 8;
            if (now == 1) {
                Queue.get().allOrders().list().clear();
                Queue.get().clearCache();
                ConstructionRequests.constructions.clear();
                Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Pylon, 16));
                EventLog.clearForTests();
            }

            Queue.get().refresh();
            for (ProductionOrder order : new ArrayList<ProductionOrder>(
                Queue.get().readyToProduceOrders().list())) {
                ProduceOrdersFromQueue.handleProductionOrder(order);
            }
        });

        int startedBank = firstPylonStartedBank();
        assertTrue(
            startedBank >= 50,
            "no reckless dispatch below price minus headroom, got bank " + startedBank);
        assertTrue(
            startedBank < 100,
            "dispatched before the full price, not hoarded past it: bank " + startedBank);
    }

    /**
     * The BUILDING_STARTED twin is the contract (the builder field on the
     * stub construction is not set on this path - the twin carries the
     * builder id instead). -1 when no Pylon started, which fails both
     * bounds above loudly.
     */
    private static int firstPylonStartedBank() {
        String log = EventLog.toJsonLines();
        int from = 0;
        while (true) {
            int key = log.indexOf("\"key\":\"BUILDING_STARTED\"", from);
            if (key < 0) return -1;
            int end = log.indexOf('\n', key);
            if (end < 0) end = log.length();
            String row = log.substring(key, end);
            if (row.contains("\"type\":\"Pylon\"")) {
                int at = row.indexOf("\"minerals\":");
                if (at < 0) return -1;
                int digits = at + "\"minerals\":".length();
                int stop = digits;
                while (stop < row.length() && Character.isDigit(row.charAt(stop))) stop++;
                return Integer.parseInt(row.substring(digits, stop));
            }
            from = end;
        }
    }
}
