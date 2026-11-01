package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.demand.Demand;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.placement.TargetPlacement;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Where the catalogue looks, in order (migration A4g, owner #146): an
 * exact tile wins, then an explicitly set nearTo, then a named modifier
 * anchor, then anywhere. The legacy spiral read in the same order
 * (nearTo first, modifier only without near); the catalogue keeps the
 * contract while ranking the tiles itself. World-free: only the
 * no-map-needed shapes pin here (exact, nearTo-wins, unknown-modifier,
 * no-hint); anchor geography pins in {@code ModifierAnchorTest}.
 */
@Tag("production")
public class ConstraintPrecedenceTest {

    private static Construction constructionFor(ProductionOrder order) {
        Construction construction = new Construction(AUnitType.Protoss_Pylon);
        construction.setProductionOrder(order);

        return construction;
    }

    @Test
    public void exactTileWinsOverEverything() {
        ProductionOrder order = new ProductionOrder(
            AUnitType.Protoss_Gateway, APosition.create(25, 25), 10);
        order.setModifier("MAIN");
        order.markAsUsingExactPosition();

        TargetPlacement constraint = CatalogueSiteAdapter.constraintFor(
            order, constructionFor(order));

        assertEquals(TargetPlacement.Mode.EXACT_TILE, constraint.mode());
        assertEquals(25, constraint.exactTileX());
    }

    @Test
    public void explicitNearToBeatsTheModifier() {
        ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
        order.setModifier("MAIN");
        Construction construction = constructionFor(order);
        construction.setNearTo(APosition.create(30, 30));

        TargetPlacement constraint = CatalogueSiteAdapter.constraintFor(order, construction);

        assertEquals(TargetPlacement.Mode.NEIGHBOURHOOD, constraint.mode());
        assertEquals(30, constraint.tileX());
    }

    @Test
    public void unknownModifierMeansAnywhere() {
        ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
        order.setModifier("NO_SUCH_MODIFIER");

        TargetPlacement constraint = CatalogueSiteAdapter.constraintFor(
            order, constructionFor(order));

        assertEquals(TargetPlacement.Mode.ANYWHERE, constraint.mode());
    }

    @Test
    public void noHintMeansAnywhere() {
        ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);

        TargetPlacement constraint = CatalogueSiteAdapter.constraintFor(
            order, constructionFor(order));

        assertEquals(TargetPlacement.Mode.ANYWHERE, constraint.mode());
    }

    @Test
    public void demandExactWins() {
        Construction construction = new Construction(AUnitType.Protoss_Pylon);
        construction.setProductionOrder(new ProductionOrder(AUnitType.Protoss_Pylon, 0));
        Demand demand = Demand.exact(AUnitType.Protoss_Pylon, APosition.create(25, 25));

        TargetPlacement constraint = CatalogueSiteAdapter.constraintFor(demand, construction);

        assertEquals(TargetPlacement.Mode.EXACT_TILE, constraint.mode());
        assertEquals(25, constraint.exactTileX());
    }

    @Test
    public void demandWithoutHintsMeansAnywhere() {
        Construction construction = new Construction(AUnitType.Protoss_Pylon);
        construction.setProductionOrder(new ProductionOrder(AUnitType.Protoss_Pylon, 0));
        Demand demand = Demand.of(AUnitType.Protoss_Pylon);

        TargetPlacement constraint = CatalogueSiteAdapter.constraintFor(demand, construction);

        assertEquals(TargetPlacement.Mode.ANYWHERE, constraint.mode());
    }
}
