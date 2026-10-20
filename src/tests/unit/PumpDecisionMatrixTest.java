package tests.unit;

import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The worker pump is a chain of vetoes - every historical stall was one
 * more gate nobody saw firing. This matrix locks the whole chain in one
 * place: each canonical opening state names its outcome AND its reason,
 * including precedence where two gates could fire. A new gate, a moved
 * gate, or a regressed gate fails here first - never a third live game
 * of the same stall.
 *
 * <p>Not covered (stub limits, documented not hidden): RushPriority
 * (needs a rush Strategy), BoardEmergency (needs hidden-enemy intel),
 * Limit (needs base train-queue stubbing), FollowBO (needs reservations
 * the stub does not build), MineralsMargin (needs 20 Zerg-facing
 * workers).</p>
 */
@Tag("production")
public class PumpDecisionMatrixTest extends AbstractTestWithWorld {

    private String lastReason = null;
    private boolean lastFlow = false;

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

    @AfterEach
    public void clean() {
        atlantis.production.constructions.ConstructionRequests.constructions.clear();
    }

    private static FakeUnit[] probes(FakeUnit nexus, int count) {
        List<FakeUnit> units = new ArrayList<FakeUnit>();
        units.add(nexus);
        for (int i = 0; i < count; i++) {
            units.add(fake(AUnitType.Protoss_Probe, 21 + (i % 4), 21 + (i / 4)));
        }
        FakeUnit[] array = new FakeUnit[units.size()];
        return units.toArray(array);
    }

    private void decide(int supplyUsed, int supplyTotal, int minerals, FakeUnit[] ours,
            List<ProductionOrder> rows) {
        lastReason = null;
        AutoProduceWorkersCommander.REASON = "-";
        currentMinerals = minerals;
        currentGas = 100;
        currentSupplyUsed = supplyUsed;
        currentSupplyTotal = supplyTotal;

        world(5, fakeOurs(ours), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || lastReason != null) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            for (ProductionOrder row : rows) {
                Queue.get().addNew(0, row);
            }
            Queue.get().clearCache();

            lastFlow = AutoProduceWorkersCommander.shouldProduceWorkers();
            lastReason = AutoProduceWorkersCommander.REASON;
        });
        assertTrue(lastReason != null || lastFlow, "the frame was reached");
    }

    private static ProductionOrder row(AUnitType type, int minSupply) {
        return new ProductionOrder(type, minSupply);
    }

    private static ProductionOrder highReady(AUnitType type, int minSupply) {
        ProductionOrder order = new ProductionOrder(type, minSupply);
        order.setPriority(ProductionOrderPriority.HIGH);
        order.setStatus(OrderStatus.READY_TO_PRODUCE);
        return order;
    }

    @Test
    public void blockedBaseBuysNothing() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        decide(12, 12, 100, probes(nexus, 5), new ArrayList<ProductionOrder>());
        assertEquals("NoSupply", lastReason);
        assertTrue(!lastFlow, "a blocked base trains nothing");
    }

    @Test
    public void poorBaseBuysNothing() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        decide(9, 18, 30, probes(nexus, 5), new ArrayList<ProductionOrder>());
        assertEquals("CantAfford", lastReason);
    }

    @Test
    public void noPylonBeatsWaiter() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        List<ProductionOrder> rows = new ArrayList<ProductionOrder>();
        rows.add(highReady(AUnitType.Protoss_Gateway, 10));
        decide(9, 18, 100, probes(nexus, 5), rows);
        assertEquals("PylonAt8", lastReason);
    }

    @Test
    public void waiterStranglesWithoutMandate() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ours = new FakeUnit[]{
            nexus,
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
        };
        List<ProductionOrder> rows = new ArrayList<ProductionOrder>();
        rows.add(highReady(AUnitType.Protoss_Gateway, 10));
        decide(14, 17, 140, ours, rows);
        assertEquals("HigherPriorityWaits", lastReason);
        assertTrue(!lastFlow, "a 60 shortfall still saves");
    }

    @Test
    public void mandateFlowsAtNine() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ours = new FakeUnit[]{
            nexus,
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
        };
        List<ProductionOrder> rows = new ArrayList<ProductionOrder>();
        rows.add(row(AUnitType.Protoss_Gateway, 10));
        decide(9, 18, 100, ours, rows);
        assertTrue(lastFlow, "supply 9, Pylon standing, Gateway due: mandatory");
    }

    @Test
    public void coreSavingOutranksMandate() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ours = new FakeUnit[]{
            nexus,
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
        };
        List<ProductionOrder> rows = new ArrayList<ProductionOrder>();
        rows.add(row(AUnitType.Protoss_Gateway, 10));
        rows.add(row(AUnitType.Protoss_Cybernetics_Core, 9));
        decide(9, 18, 100, ours, rows);
        assertEquals("CriticalStuff", lastReason);
        assertTrue(!lastFlow, "the mandate bypasses the waiter gate only");
    }

    @Test
    public void maxedBaseBuysNothing() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        decide(90, 100, 500, probes(nexus, 80), new ArrayList<ProductionOrder>());
        assertEquals("MaxedWorkers", lastReason);
    }
}
