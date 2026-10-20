package tests.unit;

import atlantis.map.position.APosition;
import atlantis.map.scout.ScoutState;
import atlantis.map.scout.ScoutTerminalAnchor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Idle-fallback roam (owner direction): when no enemy-anchored location is
 * defined, the terminal anchor roams enemy starts instead of idling - early
 * (enemy never discovered) cycling 1st/2nd/3rd in fixed order, later
 * randomly. Pins the cycle order and the random branch's membership;
 * the manager wiring (anchors first, roam only when undefined) is covered
 * by ScoutTerminalAnchorTest + the chain.
 */
@Tag("map")
public class ScoutTerminalRoamTest {

    private static final APosition FIRST = APosition.create(10, 10);
    private static final APosition SECOND = APosition.create(60, 60);
    private static final APosition THIRD = APosition.create(110, 110);

    @AfterEach
    public void resetRotation() {
        ScoutState.reset();
    }

    @Test
    public void earlyGameCyclesFirstSecondThirdInOrder() {
        ArrayList<APosition> starts = new ArrayList<>(Arrays.asList(FIRST, SECOND, THIRD));

        assertEquals(FIRST, ScoutTerminalAnchor.nextRoamAnchor(starts, true));
        assertEquals(SECOND, ScoutTerminalAnchor.nextRoamAnchor(starts, true));
        assertEquals(THIRD, ScoutTerminalAnchor.nextRoamAnchor(starts, true));
        assertEquals(FIRST, ScoutTerminalAnchor.nextRoamAnchor(starts, true));
    }

    @Test
    public void lateGameStaysInsideCandidates() {
        ArrayList<APosition> starts = new ArrayList<>(Arrays.asList(FIRST, SECOND, THIRD));
        HashSet<APosition> set = new HashSet<>(starts);

        for (int i = 0; i < 10; i++) {
            assertTrue(
                set.contains(ScoutTerminalAnchor.nextRoamAnchor(starts, false)),
                "random roam must still name an enemy start, never nowhere");
        }
    }

    @Test
    public void noStartsIsNull() {
        assertNull(ScoutTerminalAnchor.nextRoamAnchor(new ArrayList<APosition>(), true));
        assertNull(ScoutTerminalAnchor.nextRoamAnchor(new ArrayList<APosition>(), false));
        assertNull(ScoutTerminalAnchor.nextRoamAnchor(null, true));
    }

    @Test
    public void singleCandidateAlwaysIt() {
        assertEquals(
            FIRST,
            ScoutTerminalAnchor.nextRoamAnchor(Collections.singletonList(FIRST), false));
    }
}
