package tests.unit;

import atlantis.production.constructions.NewConstructionRequest;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-17 (Phase 4): a power verdict pulls a Pylon instead of
 * churning. Pure predicate on (dominant refusal, building, order) - the
 * side effect (one pylon near the area, proximity-guarded) runs only on
 * true. Physical failures never pull: no pylon fixes a full map.
 */
@Tag("production")
public class PowerFailurePullsPylonTest {

    private ProductionOrder order() {
        return new ProductionOrder(AUnitType.Protoss_Gateway, 12);
    }

    @Test
    public void powerDominantPulls() {
        assertTrue(NewConstructionRequest.shouldPullPylon(
            AUnitType.Protoss_Gateway, "Not powered by a Pylon x88 (of 100)", order()));
        assertTrue(NewConstructionRequest.shouldPullPylon(
            AUnitType.Protoss_Gateway, "Not powered by a Pylon", order()));
    }

    @Test
    public void physicalNeverPulls() {
        assertFalse(NewConstructionRequest.shouldPullPylon(
            AUnitType.Protoss_Gateway, "Can't physically build here x429 (of 505)", order()));
    }

    @Test
    public void powerFreeBuildingsAndMissingContextNeverPull() {
        assertFalse(NewConstructionRequest.shouldPullPylon(
                AUnitType.Protoss_Pylon, "Not powered by a Pylon x5", order()),
            "pylons need no power - pulling one for power is nonsense");
        assertFalse(NewConstructionRequest.shouldPullPylon(
            AUnitType.Protoss_Gateway, "Not powered by a Pylon x5", null));
        assertFalse(NewConstructionRequest.shouldPullPylon(
            AUnitType.Protoss_Gateway, null, order()));
        assertFalse(NewConstructionRequest.shouldPullPylon(
            null, "Not powered by a Pylon x5", order()));
    }
}
