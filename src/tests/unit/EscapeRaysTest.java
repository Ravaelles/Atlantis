package tests.unit;

import atlantis.battle.escape.EscapeRays;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Escape slice 2 (NEXT #174): 8-direction walkable clearance is pure
 * marching over a caller-supplied predicate - no map needed.
 */
@Tag("combat")
public class EscapeRaysTest {

    @Test
    public void openGroundReadsMaxEverywhere() {
        double[] clear = EscapeRays.clearance(APosition.create(30, 20), 12, (x, y) -> true);

        for (double c : clear) assertEquals(12.0, c, 0.001);
    }

    @Test
    public void wallStopsOnlyItsRays() {
        // Wall straight east of (30, 20): E/SE/NE clip after 1 tile, the rest run full.
        double[] clear = EscapeRays.clearance(APosition.create(30, 20), 12, (x, y) -> x < 32);

        assertEquals(1.0, clear[0], 0.001, "east");
        assertEquals(1.0, clear[1], 0.001, "south-east clips on the corner");
        assertEquals(1.0, clear[7], 0.001, "north-east clips on the corner");
        assertTrue(clear[4] == 12.0, "west runs full");
        assertTrue(clear[2] == 12.0, "south runs full");
    }

    @Test
    public void boxedInReadsZeroEverywhere() {
        double[] clear = EscapeRays.clearance(APosition.create(30, 20), 12, (x, y) -> false);

        for (double c : clear) assertEquals(0.0, c, 0.001);
    }

    @Test
    public void nullInputGivesZerosNeverAnException() {
        double[] clear = EscapeRays.clearance(null, 12, (x, y) -> true);

        for (double c : clear) assertEquals(0.0, c, 0.001);
    }
}
