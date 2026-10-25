package tests.unit;

import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.focus.PostureReason;
import atlantis.combat.focus.PostureReasons;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NEXT #197: the posture answers once, per frame, with a named reason.
 * Pure decision pins (no game): the headcount rule stands, the trapped
 * and banked shapes from the old mission changer have their homes, and
 * every threshold edge reads exactly the calibrated line.
 */
@Tag("combat")
public class PostureReasonsTest {

    private static PostureReasons.Verdict decide(
        int ours, int dragoons, int zealots, int minerals, int seconds
    ) {
        return PostureReasons.decide(ours, dragoons, zealots, minerals, seconds);
    }

    @Test
    public void headcountRuleStands() {
        PostureReasons.Verdict up = decide(8, 0, 0, 0, 100);
        assertEquals(ClusterMission.Posture.ATTACK, up.posture);
        assertEquals(PostureReason.HEADCOUNT, up.reason);

        PostureReasons.Verdict down = decide(7, 0, 0, 0, 100);
        assertEquals(ClusterMission.Posture.DEFEND, down.posture);
        assertEquals(PostureReason.HOLD_GROUND, down.reason);
    }

    @Test
    public void trappedForceMarchesOut() {
        PostureReasons.Verdict verdict = decide(4, 2, 6, 0, 100);

        assertEquals(ClusterMission.Posture.ATTACK, verdict.posture);
        assertEquals(PostureReason.TRAPPED_BY_MASS_ZEALOTS, verdict.reason);
    }

    @Test
    public void trappedNeedsTheWholeShape() {
        assertEquals(PostureReason.HOLD_GROUND, decide(3, 2, 6, 0, 100).reason,
            "below the measured floor the old behavior stands");
        assertEquals(PostureReason.HOLD_GROUND, decide(4, 1, 6, 0, 100).reason,
            "a lone dragoon holds no kite line");
        assertEquals(PostureReason.HOLD_GROUND, decide(4, 2, 5, 0, 100).reason,
            "below the mass line holding wins (sim-bracketed)");
        assertEquals(PostureReason.HOLD_GROUND, decide(4, 2, 6, 0, 651).reason,
            "late game has tech answers, not this one");
    }

    @Test
    public void bankedNamesTheSpendPressure() {
        PostureReasons.Verdict verdict = decide(12, 4, 0, 1500, 100);

        assertEquals(ClusterMission.Posture.ATTACK, verdict.posture);
        assertEquals(PostureReason.BANKED_MINERALS, verdict.reason);
    }

    @Test
    public void bankedNeverFlipsASmallArmyAlone() {
        assertEquals(ClusterMission.Posture.DEFEND, decide(6, 2, 0, 1500, 100).posture,
            "no provenance for blind small-army banked attacks");
        assertEquals(PostureReason.HEADCOUNT, decide(12, 4, 0, 999, 100).reason,
            "below the bank it is the plain headcount");
    }
}
