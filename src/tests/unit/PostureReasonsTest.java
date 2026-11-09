package tests.unit;

import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.focus.PostureReason;
import atlantis.combat.focus.PostureReasons;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
        PostureReasons.Input in = new PostureReasons.Input();
        in.ours = ours;
        in.ourDragoons = dragoons;
        in.enemyZealots = zealots;
        in.minerals = minerals;
        in.gameSeconds = seconds;

        return PostureReasons.decide(in);
    }

    private static PostureReasons.Verdict decideStrong(
        int ours, int dragoons, int strength, boolean dragoonMatchup, boolean wasAttacking, String matchup
    ) {
        return decideFull(ours, dragoons, strength, dragoonMatchup, wasAttacking, matchup, false, 0, 1.0, false);
    }

    private static PostureReasons.Verdict decideFull(
        int ours, int dragoons, int strength, boolean dragoonMatchup, boolean wasAttacking, String matchup,
        boolean vsTerran, int enemyDragoons, double eval, boolean vsZerg
    ) {
        PostureReasons.Input in = new PostureReasons.Input();
        in.ours = ours;
        in.ourDragoons = dragoons;
        in.gameSeconds = 100;
        in.strength = strength;
        in.dragoonMatchup = dragoonMatchup;
        in.wasAttacking = wasAttacking;
        in.matchup = matchup;
        in.vsTerran = vsTerran;
        in.enemyDragoons = enemyDragoons;
        in.eval = eval;
        in.vsZerg = vsZerg;

        return PostureReasons.decide(in);
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
    public void terranEarlyPressureFromTwoUnitsOrOneGoon() {
        assertEquals(PostureReason.EARLY_TERRAN_PRESSURE,
            decideFull(2, 0, 200, false, false, "vT", true, 0, 1.0, false).reason,
            "vs Terran two units march");
        assertEquals(PostureReason.EARLY_TERRAN_PRESSURE,
            decideFull(1, 1, 200, false, false, "vT", true, 0, 1.0, false).reason,
            "vs Terran one finished Dragoon marches");
        assertEquals(PostureReason.HOLD_GROUND,
            decideFull(2, 0, 100, false, false, "vT", true, 0, 1.0, false).reason,
            "the shared strength floor holds even vs Terran");
        assertEquals(PostureReason.HOLD_GROUND,
            decideFull(1, 0, 999, false, false, "vT", true, 0, 1.0, false).reason,
            "one zealot is not pressure");
    }

    @Test
    public void mirrorDragoonsSendSmallLinesHome() {
        PostureReasons.Verdict verdict = decideFull(3, 2, 300, true, true, "vP", false, 2, 1.0, false);

        assertEquals(ClusterMission.Posture.DEFEND, verdict.posture);
        assertEquals(PostureReason.ENEMY_DRAGOONS_SO_COME_HOME, verdict.reason);
    }

    @Test
    public void mirrorDragoonsReleaseOnSuperiorityOrMass() {
        assertEquals(PostureReason.EARLY_STRENGTH,
            decideFull(3, 1, 300, true, true, "vP", false, 2, 1.3, false).reason,
            "eval 1.3+ lets the composition decide");
        assertEquals(PostureReason.EARLY_DRAGOONS_OK_SO_PRESSURE,
            decideFull(4, 4, 300, true, true, "vP", false, 2, 1.0, false).reason,
            "4 own Dragoons decide for themselves");
        assertEquals(PostureReason.HOLD_GROUND,
            decideFull(3, 2, 100, true, false, "vZ", false, 0, 1.0, false).reason,
            "without enemy dragoons there is nothing to mirror");
    }

    @Test
    public void zealotsHoldVsZergWithoutDragoons() {
        assertEquals(PostureReason.ZEALOTS_HOLD_VS_ZERG,
            decideFull(4, 0, 100, false, false, "vZ", false, 0, 1.0, true).reason,
            "zealots leave only at 150+ or with a dragoon");
        assertEquals(PostureReason.ZEALOTS_HOLD_VS_ZERG,
            decideFull(4, 0, 149, false, false, "vZ", false, 0, 1.0, true).reason,
            "149 is still holding");
        assertEquals(PostureReason.EARLY_STRENGTH,
            decideFull(4, 0, 150, false, false, "vZ", false, 0, 1.0, true).reason,
            "150 opens the march");
        assertEquals(PostureReason.EARLY_DRAGOONS_OK_SO_PRESSURE,
            decideFull(4, 2, 200, true, false, "vZ", false, 0, 1.0, true).reason,
            "a dragoon in the force releases the zealots");
        assertEquals(PostureReason.HOLD_GROUND,
            decideFull(4, 0, 100, false, false, "vP", false, 0, 1.0, false).reason,
            "not vs Zerg, not this rule");
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
        assertEquals("Zealots hold vs Zerg (100%, no dragoon)",
            decideFull(4, 0, 100, false, false, "vZ", false, 0, 1.0, true).text);
        assertEquals("Early Terran pressure (2 units, 200%)",
            decideFull(2, 0, 200, false, false, "vT", true, 0, 1.0, false).text);
        assertEquals("Enemy dragoons (2 goons, eval 1.0), coming home",
            decideFull(3, 2, 300, true, true, "vP", false, 2, 1.0, false).text);
    }

    @Test
    public void protossMassMarchesWithoutContact() {
        // Owner matchup doctrine: vs Protoss with no contact, a line with
        // teeth marches instead of holding for blind strength.
        PostureReasons.Input in = new PostureReasons.Input();
        in.ours = 4;
        in.ourDragoons = 4;
        in.ourZealots = 0;
        in.vsProtoss = true;
        in.enemyKnown = false;
        in.strength = 999;
        in.gameSeconds = 100;

        PostureReasons.Verdict verdict = PostureReasons.decide(in);

        assertEquals(ClusterMission.Posture.ATTACK, verdict.posture);
        assertEquals(PostureReason.PROTOSS_MASS_READY, verdict.reason);
    }

    @Test
    public void protossToothlessLineWaitsEvenBlind() {
        // Same blindness, no teeth: holding is the doctrine, not a weak
        // blind march - 2 zealots see nothing and stay home.
        PostureReasons.Input in = new PostureReasons.Input();
        in.ours = 2;
        in.ourDragoons = 0;
        in.ourZealots = 2;
        in.vsProtoss = true;
        in.enemyKnown = false;
        in.strength = 999;
        in.gameSeconds = 100;

        PostureReasons.Verdict verdict = PostureReasons.decide(in);

        assertEquals(ClusterMission.Posture.DEFEND, verdict.posture);
        assertEquals(PostureReason.HOLD_GROUND, verdict.reason);
    }

    @Test
    public void protossContactStillTrapsDespiteMass() {
        // The gate never eats contact shapes: massed Dragoons chewing
        // zealots still read TRAPPED, not mass-ready.
        PostureReasons.Input in = new PostureReasons.Input();
        in.ours = 4;
        in.ourDragoons = 4;
        in.ourZealots = 0;
        in.vsProtoss = true;
        in.enemyKnown = true;
        in.enemyZealots = 6;
        in.strength = 200;
        in.gameSeconds = 100;

        PostureReasons.Verdict verdict = PostureReasons.decide(in);

        assertEquals(ClusterMission.Posture.ATTACK, verdict.posture);
        assertEquals(PostureReason.TRAPPED_BY_MASS_ZEALOTS, verdict.reason);
    }

    @Test
    public void techTurtleHoldsWithoutDragoon() {
        PostureReasons.Input in = new PostureReasons.Input();
        in.ours = 6;
        in.ourDragoons = 0;
        in.strength = 200;
        in.goingTech = true;
        in.matchup = "vZ";

        PostureReasons.Verdict verdict = PostureReasons.decide(in);

        assertEquals(ClusterMission.Posture.DEFEND, verdict.posture);
        assertEquals(PostureReason.TECH_TURTLE, verdict.reason);
    }

    @Test
    public void techTurtleReleasesPast225() {
        PostureReasons.Input in = new PostureReasons.Input();
        in.ours = 6;
        in.ourDragoons = 0;
        in.strength = 226;
        in.goingTech = true;
        in.matchup = "vZ";

        assertFalse(PostureReasons.decide(in).reason == PostureReason.TECH_TURTLE,
            "above 225 the normal attack reasons own the board");
    }

    @Test
    public void techTurtleNeedsZeroDragoons() {
        PostureReasons.Input in = new PostureReasons.Input();
        in.ours = 6;
        in.ourDragoons = 1;
        in.strength = 200;
        in.goingTech = true;
        in.matchup = "vZ";

        assertFalse(PostureReasons.decide(in).reason == PostureReason.TECH_TURTLE,
            "one Dragoon is teeth: no turtle");
    }

    @Test
    public void noTechNoTurtle() {
        PostureReasons.Input in = new PostureReasons.Input();
        in.ours = 6;
        in.ourDragoons = 0;
        in.strength = 200;
        in.goingTech = false;
        in.matchup = "vZ";

        assertFalse(PostureReasons.decide(in).reason == PostureReason.TECH_TURTLE,
            "not going tech: no turtle");
    }

    private static PostureReasons.Input rangedMassBase() {
        PostureReasons.Input in = new PostureReasons.Input();
        in.ours = 6;
        in.ourDragoons = 2;
        in.strength = 80;
        in.gameSeconds = 300;
        in.wasAttacking = true;
        in.enemyRanged = 2;
        in.ums = false;
        in.matchup = "vP";

        return in;
    }

    @Test
    public void rangedMassEndsTheAttack() {
        PostureReasons.Verdict verdict = PostureReasons.decide(rangedMassBase());

        assertEquals(ClusterMission.Posture.DEFEND, verdict.posture);
        assertEquals(PostureReason.RANGED_MASS_SO_COME_HOME, verdict.reason);
    }

    @Test
    public void rangedMassNamesItselfBeforeTheGeneralFallback() {
        PostureReasons.Input in = rangedMassBase();
        in.ourDragoons = 0;

        assertEquals(PostureReason.RANGED_MASS_SO_COME_HOME, PostureReasons.decide(in).reason,
            "the sharper cause wins over OUTGUNNED_SO_COME_HOME (both would fire)");
    }

    @Test
    public void rangedMassNeedsTheWholeShape() {
        PostureReasons.Input strong = rangedMassBase();
        strong.strength = 86;
        assertFalse(PostureReasons.decide(strong).reason == PostureReason.RANGED_MASS_SO_COME_HOME,
            "above 85 the general rules own the board");

        PostureReasons.Input few = rangedMassBase();
        few.enemyRanged = 1;
        assertFalse(PostureReasons.decide(few).reason == PostureReason.RANGED_MASS_SO_COME_HOME,
            "one raider is not a mass");

        PostureReasons.Input late = rangedMassBase();
        late.gameSeconds = 421;
        assertFalse(PostureReasons.decide(late).reason == PostureReason.RANGED_MASS_SO_COME_HOME,
            "past 7 minutes the line has other answers");

        PostureReasons.Input peace = rangedMassBase();
        peace.wasAttacking = false;
        assertFalse(PostureReasons.decide(peace).reason == PostureReason.RANGED_MASS_SO_COME_HOME,
            "ends attacks, never prevents them");

        PostureReasons.Input ums = rangedMassBase();
        ums.ums = true;
        assertFalse(PostureReasons.decide(ums).reason == PostureReason.RANGED_MASS_SO_COME_HOME,
            "UMS plays by its own rules");
    }

    private static PostureReasons.Input rushBase() {
        PostureReasons.Input in = new PostureReasons.Input();
        in.ours = 5;
        in.ourDragoons = 0;
        in.enemyDragoons = 0;
        in.strength = 100;
        in.gameSeconds = 200;
        in.enemyRushOrCheese = true;
        in.wasAttacking = false;
        in.matchup = "vP";

        return in;
    }

    @Test
    public void rushDefenceHoldsEarly() {
        PostureReasons.Verdict verdict = PostureReasons.decide(rushBase());

        assertEquals(ClusterMission.Posture.DEFEND, verdict.posture);
        assertEquals(PostureReason.RUSH_DEFENCE, verdict.reason);
    }

    @Test
    public void rushDefenceBeatsTheHeadcountMarch() {
        PostureReasons.Input big = rushBase();
        big.ours = 9;

        assertEquals(PostureReason.RUSH_DEFENCE, PostureReasons.decide(big).reason,
            "a rusher with 9 units still holds: the writer forced DEFEND at any size");
    }

    @Test
    public void rushDefenceNeedsTheWholeShape() {
        PostureReasons.Input calm = rushBase();
        calm.enemyRushOrCheese = false;
        assertFalse(PostureReasons.decide(calm).reason == PostureReason.RUSH_DEFENCE,
            "no rush, no answer");

        PostureReasons.Input late = rushBase();
        late.gameSeconds = 361;
        assertFalse(PostureReasons.decide(late).reason == PostureReason.RUSH_DEFENCE,
            "past 6 minutes other doctrines own the board");

        PostureReasons.Input strong = rushBase();
        strong.strength = 140;
        assertFalse(PostureReasons.decide(strong).reason == PostureReason.RUSH_DEFENCE,
            "much stronger means the rush failed: march");

        PostureReasons.Input goons = rushBase();
        goons.vsProtoss = true;
        goons.ourDragoons = 3;
        goons.enemyDragoons = 2;
        assertFalse(PostureReasons.decide(goons).reason == PostureReason.RUSH_DEFENCE,
            "goons outmassing the raiders 1.5x ignore the rush");
    }
}
