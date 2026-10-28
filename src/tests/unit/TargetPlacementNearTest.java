package tests.unit;

import atlantis.production.v2.TargetPlacement;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NEXT #214a: the CATALOGUE_SITE answer names WHERE it was asked, not
 * just WHAT tile it picked. The constraint owns that translation: a
 * neighbourhood centres on its tile, an exact order on its pin,
 * anything else names nothing (-1).
 */
@Tag("production")
public class TargetPlacementNearTest {

    @Test
    public void neighbourhoodNamesItsCentre() {
        TargetPlacement constraint = TargetPlacement.inNeighbourhood(20, 21);

        assertEquals(20, constraint.nearTileX());
        assertEquals(21, constraint.nearTileY());
    }

    @Test
    public void exactOrderNamesItsPin() {
        TargetPlacement constraint = TargetPlacement.exactTile(25, 26);

        assertEquals(25, constraint.nearTileX());
        assertEquals(26, constraint.nearTileY());
    }

    @Test
    public void anywhereNamesNothing() {
        TargetPlacement constraint = TargetPlacement.anywhere();

        assertEquals(-1, constraint.nearTileX());
        assertEquals(-1, constraint.nearTileY());
    }

    @Test
    public void namedAreaNamesNoTile() {
        TargetPlacement constraint = TargetPlacement.namedArea("MAIN");

        assertEquals(-1, constraint.nearTileX());
        assertEquals(-1, constraint.nearTileY());
    }
}
