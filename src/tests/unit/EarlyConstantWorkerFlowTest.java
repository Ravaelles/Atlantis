package tests.unit;

import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
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
 * Owner 2026-10-16: with a fresh Pylon at SupplyTotal 10-20 the worker
 * pump must flow even with something else queued. Measured (Wine): a
 * HIGH Gateway waiter (+Core behind it) strangled workers toward a 400
 * bank while the Gateway itself never started. A shortfall of at most 24
 * minerals does not stop the pump - the worker pays itself back; a
 * bigger gap still saves.
 */
@Tag("production")
public class EarlyConstantWorkerFlowTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    private void waiterPosture(int minerals) {
        currentMinerals = minerals;
        currentGas = 100;
        currentSupplyUsed = 14;
        currentSupplyTotal = 17;
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();

        ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
        gateway.setPriority(ProductionOrderPriority.HIGH);
        gateway.setStatus(OrderStatus.READY_TO_PRODUCE);
        Queue.get().addNew(0, gateway);
        Queue.get().clearCache();
    }

    @Test
    public void shortfallUpTo24Flows() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 21, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            waiterPosture(190);

            assertTrue(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "50 + 150 waiter against 190 bank is a 10 shortfall - the "
                    + "pump flows, got: " + AutoProduceWorkersCommander.REASON);
        });
    }

    @Test
    public void biggerGapStillSaves() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 21, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            waiterPosture(140);

            assertFalse(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "50 + 150 waiter against 140 bank is a 60 shortfall - save");
            assertEquals(
                "HigherPriorityWaits", AutoProduceWorkersCommander.REASON,
                "beyond the 24 band the old gate stands with its old name");
        });
    }

    @Test
    public void outsideTheWindowNormalRulesApply() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 21, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            currentMinerals = 190;
            currentGas = 100;
            currentSupplyUsed = 14;
            currentSupplyTotal = 30;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            gateway.setPriority(ProductionOrderPriority.HIGH);
            gateway.setStatus(OrderStatus.READY_TO_PRODUCE);
            Queue.get().addNew(0, gateway);
            Queue.get().clearCache();

            assertFalse(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "SupplyTotal 30 is past the early window - the 10 shortfall saves");
        });
    }
}
