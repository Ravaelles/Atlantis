package tests.unit;

import atlantis.map.position.APosition;
import atlantis.map.scout.ScoutTerminalAnchor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Owner 2026-10-16: a scout never stands idle while any enemy base location
 * is defined. The terminal anchor revisits explored anchors oldest-first
 * (natural, third, fourth, main) once the unexplored-first managers
 * decline - only absent map data may still silence the chain.
 */
@Tag("map")
public class ScoutTerminalAnchorTest {

    private static final APosition NATURAL = APosition.create(100, 20);
    private static final APosition THIRD = APosition.create(60, 60);
    private static final APosition FOURTH = APosition.create(20, 100);
    private static final APosition MAIN = APosition.create(10, 110);

    @Test
    public void exploredAnchorsStillAnchor() {
        assertEquals(NATURAL, ScoutTerminalAnchor.firstDefined(NATURAL, THIRD, FOURTH, MAIN));
        assertEquals(THIRD, ScoutTerminalAnchor.firstDefined(null, THIRD, FOURTH, MAIN));
        assertEquals(FOURTH, ScoutTerminalAnchor.firstDefined(null, null, FOURTH, MAIN));
        assertEquals(MAIN, ScoutTerminalAnchor.firstDefined(null, null, null, MAIN));
    }

    @Test
    public void noMapDataIsNull() {
        assertNull(
            ScoutTerminalAnchor.firstDefined(null, null, null, null),
            "no enemy base location defined anywhere - the one case the "
                + "commander's idle line still names");
    }
}
