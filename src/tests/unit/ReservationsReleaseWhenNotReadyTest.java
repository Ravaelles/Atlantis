package tests.unit;

import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import tests.unit.AbstractTestWithUnits;

/**
 * NEXT #89: reservations attach when an order turns READY but never release
 * when it flips back to NOT_READY - every READY/NOT_READY flap (bank
 * sawtooth vs reserved pool) inflates requirements permanently. Game 180053:
 * reserved=300 stuck while Gateway-2 (150) waited with an effective bank
 * under 150 and zero overdue-cancels. A not-ready order holds no money.
 */
@Tag("infra")
public class ReservationsReleaseWhenNotReadyTest extends AbstractTestWithUnits {

    @Test
    public void notReadyReleasesReservation() {
        int before = ReservedResources.minerals();

        ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 0);
        gateway.setStatus(OrderStatus.READY_TO_PRODUCE);
        assertEquals(
            before + 150, ReservedResources.minerals(),
            "sanity: READY reserves the price");

        gateway.setStatus(OrderStatus.NOT_READY);
        assertEquals(
            before, ReservedResources.minerals(),
            "leaving READY must release - the money is not earmarked anymore");
    }
}
