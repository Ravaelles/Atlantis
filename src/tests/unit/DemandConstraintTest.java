package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.production.demand.Demand;
import atlantis.production.placement.TargetPlacement;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Demand-to-constraint translation (NEXT #228, WO-147): the same three
 * shapes the order overload pins in CatalogueSiteAdapterTest, read from
 * the v2 carrier. Pure translation, no map physics.
 */
@Tag("production")
public class DemandConstraintTest {

    @Test
    public void neighbourhoodDemandStaysNeighbourhood() {
        Construction construction = new Construction(AUnitType.Protoss_Pylon);
        construction.setNearTo(APosition.create(20, 20));

        TargetPlacement constraint = CatalogueSiteAdapter.constraintFor(
            Demand.of(AUnitType.Protoss_Pylon), construction);

        assertEquals(TargetPlacement.Mode.NEIGHBOURHOOD, constraint.mode());
        assertEquals(20, constraint.tileX());
        assertEquals(20, constraint.tileY());
    }

    @Test
    public void exactDemandStaysExact() {
        Construction construction = new Construction(AUnitType.Protoss_Gateway);

        TargetPlacement constraint = CatalogueSiteAdapter.constraintFor(
            Demand.exact(AUnitType.Protoss_Gateway, APosition.create(25, 25)), construction);

        assertEquals(TargetPlacement.Mode.EXACT_TILE, constraint.mode());
        assertEquals(25, constraint.exactTileX());
        assertEquals(25, constraint.exactTileY());
    }

    @Test
    public void noHintMeansAnywhere() {
        Construction construction = new Construction(AUnitType.Protoss_Pylon);

        TargetPlacement constraint = CatalogueSiteAdapter.constraintFor(
            Demand.of(AUnitType.Protoss_Pylon), construction);

        assertEquals(TargetPlacement.Mode.ANYWHERE, constraint.mode());
    }
}
