package tests.unit;

import atlantis.map.scout.ScoutCommander;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * A scout whose manager chain finds nothing left to do must be released back
 * to work instead of standing idle forever (the fall-through hole: all
 * managers decline at once, no terminal fallback, no abandonment).
 */
public class ScoutReleaseTest {

    @AfterEach
    public void clearScoutStatics() {
        ScoutState.scouts.clear();
        ScoutState.reset();
    }

    @Test
    public void pendingScoutIsFreedAndFlagCleared() {
        FakeUnit scout = fake(AUnitType.Protoss_Probe, 10);
        ScoutState.scouts.add(scout);
        scout.setScout(true);
        ScoutState.scoutToRelease = scout;

        assertTrue(ScoutCommander.releasePendingScout());
        assertTrue(ScoutState.scouts.isEmpty());
        assertFalse(scout.isScout());
        assertTrue(ScoutState.scoutToRelease == null);
    }

    @Test
    public void noPendingScoutReleasesNothing() {
        assertFalse(ScoutCommander.releasePendingScout());
    }

    @Test
    public void resetClearsRoamAndReleaseState() {
        FakeUnit scout = fake(AUnitType.Protoss_Probe, 10);
        ScoutState.roamLastDistToPoint = 5.0;
        ScoutState.roamLastProgressFrame = 123;
        ScoutState.scoutToRelease = scout;

        ScoutState.reset();

        assertEquals(-1, ScoutState.roamLastDistToPoint, 0.001);
        assertEquals(0, ScoutState.roamLastProgressFrame);
        assertTrue(ScoutState.scoutToRelease == null);
    }
}
