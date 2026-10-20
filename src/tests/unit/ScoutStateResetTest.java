package tests.unit;

import atlantis.map.scout.ScoutState;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The roam progress tracking must not leak between games in one JVM.
 */
@Tag("map")
public class ScoutStateResetTest {

    @Test
    public void resetClearsRoamState() {
        ScoutState.roamLastDistToPoint = 5.0;
        ScoutState.roamLastProgressFrame = 123;
        ScoutState.terminalRoamIndex = 7;

        ScoutState.reset();

        assertEquals(-1, ScoutState.roamLastDistToPoint, 0.001);
        assertEquals(0, ScoutState.roamLastProgressFrame);
        assertEquals(0, ScoutState.terminalRoamIndex);
    }
}
