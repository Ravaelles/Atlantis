package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.v2.Demand;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Siting reads the v2 demand first, the synthetic order as fallback
 * (NEXT #228, WO-147): the director mirrors both, legacy constructions
 * carry only the order. Either source alone must answer the same.
 */
@Tag("production")
public class DemandModifierTest {

    @Test
    public void demandWinsWhenBothExist() {
        Construction construction = new Construction(AUnitType.Protoss_Pylon);
        ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 0);
        order.setModifier("ORDER_SIDE");
        construction.setProductionOrder(order);
        construction.setDemand(Demand.of(AUnitType.Protoss_Pylon, "DEMAND_SIDE", null));

        assertEquals("DEMAND_SIDE", APositionFinder.modifierFor(construction),
            "v2 demand is the primary source");
    }

    @Test
    public void legacyOrderAloneStillAnswers() {
        Construction construction = new Construction(AUnitType.Protoss_Pylon);
        ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 0);
        order.setModifier("MAIN_CHOKE");
        construction.setProductionOrder(order);

        assertEquals("MAIN_CHOKE", APositionFinder.modifierFor(construction),
            "legacy constructions carry only the order");
    }

    @Test
    public void nothingCarriedMeansNoModifier() {
        Construction construction = new Construction(AUnitType.Protoss_Pylon);

        assertNull(APositionFinder.modifierFor(construction),
            "neither source means no modifier, never an exception");
        assertNull(APositionFinder.modifierFor(null),
            "null construction means no modifier, never an exception");
    }
}
