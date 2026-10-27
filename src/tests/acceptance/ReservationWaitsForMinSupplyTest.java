package tests.acceptance;

import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-16, MANDATORY: reservations start at the row's supply,
 * never before. A Gateway row earmarking 150 at supply 9 starved the
 * worker pump toward a 200 bank while the Gateway itself waited - the
 * pump reads bank-minus-reserved, so an early earmark is an early veto
 * on everything else. Below minSupply rows wait unreserved.
 */
public class ReservationWaitsForMinSupplyTest extends WorldStubForTests {

    private boolean fired = false;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ReservedResources.reset();
        fired = false;
    }

    @Test
    public void noReservationBelowMinSupply() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;
            ReservedResources.reset();

            currentSupplyUsed = 9;
            currentSupplyTotal = 18;

            ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            gateway.setStatus(OrderStatus.READY_TO_PRODUCE);

            assertEquals(
                0, ReservedResources.minerals(),
                "supply 9 of minSupply 10: a 150 earmark here starves workers "
                    + "for a building that cannot start anyway");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void reservationFromMinSupply() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;
            ReservedResources.reset();

            currentSupplyUsed = 10;
            currentSupplyTotal = 18;

            ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            gateway.setStatus(OrderStatus.READY_TO_PRODUCE);

            assertEquals(
                150, ReservedResources.minerals(),
                "at minSupply the earmark lands - issuance math unchanged");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
