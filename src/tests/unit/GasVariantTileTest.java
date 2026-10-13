package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.builders.GasBuildingFix;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TEMPORARY experiment (NEXT #62): the attempt tile for a refinery cycles
 * across 15-frame windows (exact geyser tile, center, shifted -2,-1, shifted
 * +1,0) so a game log shows which tile the engine accepts. Pins the rotation;
 * remove with the experiment once the accepting tile is measured.
 */
public class GasVariantTileTest {

    private static final APosition GEYSER_TILE = APosition.create(86, 118);

    @Test
    public void exactTileFirst() {
        assertTile(86, 118, GasBuildingFix.experimentalVariantTile(
            GEYSER_TILE, AUnitType.Protoss_Assimilator, 0));
        assertTile(86, 118, GasBuildingFix.experimentalVariantTile(
            GEYSER_TILE, AUnitType.Protoss_Assimilator, 14));
    }

    @Test
    public void centerThenShifts() {
        // Assimilator is 4x2: center (86+2, 118+1), shifted -2,-1 and +1,0.
        assertTile(88, 119, GasBuildingFix.experimentalVariantTile(
            GEYSER_TILE, AUnitType.Protoss_Assimilator, 15));
        assertTile(84, 117, GasBuildingFix.experimentalVariantTile(
            GEYSER_TILE, AUnitType.Protoss_Assimilator, 30));
        assertTile(87, 118, GasBuildingFix.experimentalVariantTile(
            GEYSER_TILE, AUnitType.Protoss_Assimilator, 45));
    }

    private static void assertTile(int tx, int ty, APosition actual) {
        assertEquals(tx, actual.tx(), "tile x");
        assertEquals(ty, actual.ty(), "tile y");
    }
}
