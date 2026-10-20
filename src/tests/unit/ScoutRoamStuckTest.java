package tests.unit;

import atlantis.map.scout.enemy.RoamAroundEnemyBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The roam polygon used to advance only on arrival (within 1.8 tiles) - a
 * scout that can never reach its point returned the same target every frame
 * and stood still. The timeout below forces an advance instead.
 */
@Tag("map")
public class ScoutRoamStuckTest {

    @Test
    public void noProgressPastTimeoutIsStuck() {
        int now = 1000;
        int lastProgress = now - RoamAroundEnemyBase.ROAM_STUCK_TIMEOUT_FRAMES - 1;

        assertTrue(RoamAroundEnemyBase.isRoamStuck(5.0, 5.0, lastProgress, now));
    }

    @Test
    public void recentWaitIsNotStuck() {
        int now = 1000;

        assertFalse(RoamAroundEnemyBase.isRoamStuck(5.0, 5.0, now - 10, now));
    }

    @Test
    public void approachingScoutIsNotStuck() {
        int now = 1000;
        int lastProgress = now - RoamAroundEnemyBase.ROAM_STUCK_TIMEOUT_FRAMES - 1;

        assertFalse(RoamAroundEnemyBase.isRoamStuck(4.0, 5.0, lastProgress, now));
    }

    @Test
    public void untrackedPointIsNotStuck() {
        int now = 1000;

        assertFalse(RoamAroundEnemyBase.isRoamStuck(5.0, -1, 0, now));
    }
}
