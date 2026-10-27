package tests.unit;

import atlantis.game.A;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.production.orders.production.queue.updater.ReadyToProduceOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner bank rule (NEXT #202): with 550 minerals in the bank every
 * production is affordable no matter the reservations; gas reservations
 * stop mattering once the bank holds the late-game amount (300 at most
 * 150 supply, 400 above) - and only when the order actually needs gas.
 * Gas-free orders never wait on gas.
 */
@Tag("production")
public class RichBankBypassTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void clearReservations() {
        ReservedResources.reset();
    }

    @AfterEach
    public void cleanReservations() {
        ReservedResources.reset();
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().history().clear();
            Queue.get().clearCache();
        }
    }

    @Test
    public void richBankIgnoresStaleMineralReservations() {
        currentMinerals = 600;
        currentGas = 350;

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 22, 22),
            fake(AUnitType.Protoss_Cybernetics_Core, 24, 22),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (A.now() != 12) return;

            initSupply(100, 110);

            ReservedResources.reserveMinerals(500, "probe-stall");
            ReservedResources.reserveGas(300, "probe-stall");

            AUnit worker = Select.ourWorkers().first();
            ProductionOrder dragoon = new ProductionOrder(AUnitType.Protoss_Dragoon, 0);

            assertTrue(worker.isAlive());
            assertTrue(
                ReadyToProduceOrder.isReady(dragoon),
                "600/350 in the bank produces a Dragoon through 500/300 of "
                + "stale reservations - the bank, not the earmark, decides");
        });
    }

    @Test
    public void gasFreeUnitIgnoresGasReservations() {
        currentMinerals = 600;
        currentGas = 0;

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 22, 22),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (A.now() != 12) return;

            initSupply(100, 110);

            ReservedResources.reserveGas(300, "probe-stall");

            ProductionOrder zealot = new ProductionOrder(AUnitType.Protoss_Zealot, 0);

            assertTrue(
                ReadyToProduceOrder.isReady(zealot),
                "a Zealot costs no gas, so gas reservations never block it - "
                + "banked 600 minerals is the whole question");
        });
    }

    /**
     * Owner bank rule vs D3 saving (open question D10, measured 2026-10-26:
     * fails today - the HIGH waiters block the Dragoon): with 600/350 in
     * the bank a Dragoon should flow despite 600 of HIGH waiters above it.
     * Enabling this means rich bank overrides the D3 save-up doctrine,
     * which is an owner call, not a refactor. Disabled until D10 lands.
     */
    @Test
    public void richBankOverridesPriorityWaiters() {
        currentMinerals = 600;
        currentGas = 350;

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 22, 22),
            fake(AUnitType.Protoss_Cybernetics_Core, 24, 22),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (A.now() != 12) return;

            initSupply(100, 110);

            Queue.get().allOrders().list().clear();
            Queue.get().history().clear();
            for (int i = 0; i < 4; i++) {
                ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 0);
                gateway.setPriority(ProductionOrderPriority.HIGH);
                gateway.setStatus(
                    atlantis.production.orders.production.queue.order.OrderStatus.READY_TO_PRODUCE);
                Queue.get().addNew(i, gateway);
            }
            Queue.get().clearCache();

            ProductionOrder dragoon = new ProductionOrder(AUnitType.Protoss_Dragoon, 0);

            assertTrue(
                ReadyToProduceOrder.isReady(dragoon),
                "600/350 in the bank produces a Dragoon despite 600 of HIGH "
                + "waiters above it - rich bank flows, saving pauses below it");
        });
    }
}
