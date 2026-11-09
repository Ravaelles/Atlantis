package tests.unit;

import atlantis.placement.engine.SitingFailureMemo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Failure memo for the siting door (owner 2026-11-09, timeout hunt):
 * an identical failing hunt inside the TTL window answers the stored
 * refusal instead of re-scanning. Pure in, pure out - no game needed.
 */
@Tag("placement")
public class SitingFailureMemoTest {

    private static SitingFailureMemo.Key key() {
        return new SitingFailureMemo.Key("Protoss_Gateway", 100, 50, "ANYWHERE", -1);
    }

    @Test
    public void missThenHitInsideTtl() {
        SitingFailureMemo memo = new SitingFailureMemo();

        assertNull(memo.recentRefusal(key(), 1000), "nothing stored: hunt it");

        memo.noteFailure(key(), "hunt-exhausted", 1000);

        assertEquals("hunt-exhausted", memo.recentRefusal(key(), 1000 + 239),
            "same hunt inside the window: stored refusal, no re-hunt");
    }

    @Test
    public void expiryRehunts() {
        SitingFailureMemo memo = new SitingFailureMemo();
        memo.noteFailure(key(), "hunt-exhausted", 1000);

        assertNull(memo.recentRefusal(key(), 1000 + SitingFailureMemo.FAILURE_TTL_FRAMES + 1),
            "past the TTL the hunt re-runs and re-memoizes");
    }

    @Test
    public void differentCentreBucketIsADifferentHunt() {
        SitingFailureMemo memo = new SitingFailureMemo();
        memo.noteFailure(key(), "hunt-exhausted", 1000);

        SitingFailureMemo.Key far = new SitingFailureMemo.Key("Protoss_Gateway", 200, 50, "ANYWHERE", -1);
        assertNull(memo.recentRefusal(far, 1000), "another neighbourhood hunts on its own");
    }

    @Test
    public void nullRefusalIsNotStored() {
        SitingFailureMemo memo = new SitingFailureMemo();
        memo.noteFailure(key(), null, 1000);

        assertEquals(0, memo.sizeForTests(), "nothing to name on a hit: store nothing");
        assertNull(memo.recentRefusal(key(), 1000));
    }
}
