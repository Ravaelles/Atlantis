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
        return PostureReasons.decide(ours, dragoons, zealots, minerals, seconds, 0, false, false, "?");
    }

    private static PostureReasons.Verdict decideStrong(
        int ours, int dragoons, int strength, boolean dragoonMatchup, boolean wasAttacking, String matchup
    ) {
        return PostureReasons.decide(ours, dragoons, 0, 0, 100, strength, dragoonMatchup, wasAttacking, matchup);
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

    @Test
    public void earlyStrengthMarchesSmallButStrongTeams() {
        PostureReasons.Verdict verdict = decideStrong(3, 0, 140, false, false, "vZ");

        assertEquals(ClusterMission.Posture.ATTACK, verdict.posture);
        assertEquals(PostureReason.EARLY_STRENGTH, verdict.reason);
    }

    @Test
    public void earlyStrengthNeedsBothLegs() {
        assertEquals(PostureReason.HOLD_GROUND, decideStrong(3, 0, 139, false, false, "vZ").reason,
            "below 140 the small team holds");
        assertEquals(PostureReason.HOLD_GROUND, decideStrong(2, 0, 999, false, false, "vZ").reason,
            "below 3 units only the dragoon pair may flip");
    }

    @Test
    public void dragoonPairKitesInsteadOfHolding() {
        assertEquals(PostureReason.EARLY_DRAGOONS_OK_SO_PRESSURE, decideStrong(2, 2, 140, true, false, "vZ").reason,
            "vs Zerg the pair marches");
        assertEquals(PostureReason.EARLY_DRAGOONS_OK_SO_PRESSURE, decideStrong(2, 2, 140, true, false, "vP").reason,
            "vs Protoss without ranged the pair marches");
    }

    @Test
    public void dragoonPairNeedsStrengthAndMatchup() {
        assertEquals(PostureReason.HOLD_GROUND, decideStrong(2, 2, 139, true, false, "vZ").reason,
            "outgunned pairs hold even vs Zerg");
        assertEquals(PostureReason.HOLD_GROUND, decideStrong(2, 2, 999, false, false, "vZ").reason,
            "vs Protoss with (or without intel on) ranged the pair holds");
        assertEquals(PostureReason.HOLD_GROUND, decideStrong(2, 1, 999, true, false, "vZ").reason,
            "a lone dragoon is no pair");
    }

    @Test
    public void lostAttackFallsBackHome() {
        PostureReasons.Verdict verdict = decideStrong(4, 1, 100, false, true, "vZ");

        assertEquals(ClusterMission.Posture.DEFEND, verdict.posture);
        assertEquals(PostureReason.OUTGUNNED_SO_COME_HOME, verdict.reason);
    }

    @Test
    public void fallbackOnlyEndsAttacks() {
        assertEquals(PostureReason.HOLD_GROUND, decideStrong(4, 1, 100, false, false, "vZ").reason,
            "never attacking means nothing to end");
        assertEquals(PostureReason.EARLY_STRENGTH, decideStrong(4, 1, 140, false, true, "vZ").reason,
            "teeth intact at 140 keeps marching");
        assertEquals(PostureReason.HOLD_GROUND, decideStrong(2, 2, 100, true, true, "vZ").reason,
            "two dragoons are teeth: the fallback counts at most one, the weak pair holds instead");
    }

    @Test
    public void reasonsSpeakWithNumbers() {
        assertEquals("Early dragoons ok (2 goons, 140%), pressure",
            decideStrong(2, 2, 140, true, false, "vZ").text);
        assertEquals("Outgunned (1 goon, 100%), coming home",
            decideStrong(4, 1, 100, false, true, "vZ").text);
        assertEquals("Not enough Goons (0/2) vZ",
            decideStrong(3, 0, 139, false, false, "vZ").text);
        assertEquals("Too weak (50%<140%) vZ",
            decideStrong(4, 2, 50, false, false, "vZ").text);
        assertEquals("No pair pressure (ranged vP)",
            decideStrong(2, 2, 999, false, false, "vP").text);
    }
}
