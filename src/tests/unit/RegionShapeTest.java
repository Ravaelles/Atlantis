package tests.unit;

import atlantis.battle.escape.RegionShape;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Escape slice 1 (NEXT #173): the region probe is pure geometry -
 * bounding-box min dimension plus a narrow predicate.
 */
@Tag("combat")
public class RegionShapeTest {

    @Test
    public void minDimIsTheSmallerSide() {
        RegionShape shape = RegionShape.of(10, 4, APosition.create(20, 20));

        assertEquals(4.0, shape.minDimTiles, 0.001);
        assertTrue(shape.isNarrow(6));
        assertFalse(shape.isNarrow(3));
    }

    @Test
    public void openGroundIsNotNarrow() {
        RegionShape shape = RegionShape.of(90, 70, APosition.create(45, 35));

        assertFalse(shape.isNarrow(8), "main/natural scale rooms are not cramped");
    }

    @Test
    public void nullRegionGivesNullShape() {
        assertNull(RegionShape.of(null), "no map data: null, never an exception");
    }
}
