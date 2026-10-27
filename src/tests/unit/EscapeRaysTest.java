package tests.unit;

import atlantis.combat.escape.EscapeRays;
import atlantis.combat.escape.PinchRoom;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(12.0, clear[4], "west runs full");
        assertEquals(12.0, clear[2], "south runs full");
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

    @Test
    public void largeUnitsNeedWideCorridors() {
        assertTrue(EscapeRays.passable(1, false), "small slips through one tile");
        assertFalse(EscapeRays.passable(1, true), "large wedges in one tile");
        assertTrue(EscapeRays.passable(2, true), "large turns in two tiles");
    }

    @Test
    public void openGroundHasRoom() {
        assertTrue(PinchRoom.hasRoom(APosition.create(30, 20), (x, y) -> true),
            "open ground always has somewhere to kite into");
    }

    @Test
    public void boxedPocketHasNoRoom() {
        assertFalse(PinchRoom.hasRoom(APosition.create(30, 20), (x, y) -> false),
            "walls everywhere: hold and trade, don't walk into the corner");
    }

    @Test
    public void unknownWalkabilityKeepsOldBehavior() {
        assertTrue(PinchRoom.hasRoom(APosition.create(30, 20), null),
            "fail-open: the gate only blocks what it can see blocked");
    }
}
