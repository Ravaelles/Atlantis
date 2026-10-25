package tests.unit;

import atlantis.combat.focus.PostureLatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static atlantis.combat.cluster.ClusterMission.Posture.ATTACK;
import static atlantis.combat.cluster.ClusterMission.Posture.DEFEND;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NEXT #197: a flip holds long enough to march somewhere before the
 * next count flicker flips it back (both old changers had this: attack
 * 5 s strong / 8 s weak, defend 4 s). Pinned on explicit frames, no
 * game: first decision free, hold inside the window, release after,
 * backwards clock frees.
 */
@Tag("combat")
public class PostureLatchTest {

    @BeforeEach
    public void resetLatchBefore() {
        PostureLatch.resetForTests();
    }

    @AfterEach
    public void resetLatch() {
        PostureLatch.resetForTests();
    }

    @Test
    public void firstDecisionIsFree() {
        assertEquals(ATTACK, PostureLatch.hold(ATTACK, 9, 100, null, 0));
        assertEquals(DEFEND, PostureLatch.hold(DEFEND, 4, 100, null, 0));
    }

    @Test
    public void attackCommitHoldsThenReleases() {
        assertEquals(DEFEND, PostureLatch.hold(ATTACK, 9, 100, DEFEND, 0),
            "a fresh flip waits out the window");
        assertEquals(DEFEND, PostureLatch.hold(ATTACK, 9, 119, DEFEND, 0),
            "a big army holds the march 120 frames");
        assertEquals(ATTACK, PostureLatch.hold(ATTACK, 9, 120, DEFEND, 0),
            "then the march goes through");
    }

    @Test
    public void smallArmyHoldsLonger() {
        assertEquals(DEFEND, PostureLatch.hold(ATTACK, 4, 100, DEFEND, 0));
        assertEquals(DEFEND, PostureLatch.hold(ATTACK, 4, 191, DEFEND, 0),
            "a small army holds 192 frames - it flickers most");
        assertEquals(ATTACK, PostureLatch.hold(ATTACK, 4, 192, DEFEND, 0));
    }

    @Test
    public void defendCommitHoldsBriefly() {
        assertEquals(ATTACK, PostureLatch.hold(DEFEND, 9, 95, ATTACK, 0),
            "retreats wait 96 frames");
        assertEquals(DEFEND, PostureLatch.hold(DEFEND, 9, 96, ATTACK, 0));
    }

    @Test
    public void steadyPosturePassesThrough() {
        assertEquals(ATTACK, PostureLatch.hold(ATTACK, 9, 500, ATTACK, 100),
            "no flicker, no hold");
        assertEquals(DEFEND, PostureLatch.hold(DEFEND, 4, 500, DEFEND, 100));
    }

    @Test
    public void steadyFramesDoNotRearmTheClock() {
        assertEquals(ATTACK, PostureLatch.hold(ATTACK, 9, 100));
        assertEquals(ATTACK, PostureLatch.hold(ATTACK, 9, 300));
        assertEquals(DEFEND, PostureLatch.hold(DEFEND, 9, 310),
            "the commitment was made at 100, not re-armed every steady frame");
    }

    @Test
    public void backwardsClockFrees() {
        assertEquals(DEFEND, PostureLatch.hold(DEFEND, 9, 10, ATTACK, 5000),
            "a new game resets the clock - never hold forever");
    }
}
