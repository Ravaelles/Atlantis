package tests.unit;

import atlantis.information.strategy.response.protoss.ProtossResponseEnemyHiddenUnits;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The hidden-units emergency (DT/Lurker cannons + Critical cancels of
 * live tech) must not fire before hidden units can exist. Pins the
 * earliest-timing guard: measured live GAME_4382BF70, the response
 * gutted our Core + 2 Gateways at 2:37 vs Zerg for 2 phantom cannons
 * (warped 5:40/6:17 vs no Lurkers) - Lurkers need Lair + Den + research
 * (5:00+), DTs need Archives (4:30+), so any call before 4:00 is
 * spurious recognition, not defense. Pure time predicate, no game.
 */
@Tag("infra")
public class HiddenUnitsTimingGateTest {

    @Test
    public void nothingHiddenCanExistBeforeFourMinutes() {
        assertTrue(
            ProtossResponseEnemyHiddenUnits.isTooEarlyForHiddenUnits(239),
            "2:37 is the measured false-positive - Lurkers/DTs are impossible");
        assertTrue(
            ProtossResponseEnemyHiddenUnits.isTooEarlyForHiddenUnits(0),
            "game start is always too early");
    }

    @Test
    public void realTimingPassesTheGate() {
        assertFalse(
            ProtossResponseEnemyHiddenUnits.isTooEarlyForHiddenUnits(240),
            "4:00 is the earliest any hidden unit can exist");
        assertFalse(
            ProtossResponseEnemyHiddenUnits.isTooEarlyForHiddenUnits(400),
            "mid-game defense must still fire");
    }
}
