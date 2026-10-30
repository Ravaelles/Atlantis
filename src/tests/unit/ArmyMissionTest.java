package tests.unit;

import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.ClusterMission;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Mission-driven command, slice 1a (owner 2026-10-30, priority 0):
 * the cluster's job is one named decision, pinned as pure
 * input-output rows with no game. Shadow only - nothing reads the
 * answer yet, so these pins prove the vocabulary covers reality,
 * they do not change behavior.
 */
@Tag("combat")
public class ArmyMissionTest {

    private static ArmyMission.Snapshot base() {
        ArmyMission.Snapshot in = new ArmyMission.Snapshot();
        in.posture = ClusterMission.Posture.DEFEND;
        in.distToMain = 0;
        in.distToMainChoke = 30;
        in.distToNaturalChoke = Double.MAX_VALUE;
        in.distToMiddleChoke = 30;

        return in;
    }

    @Test
    public void breachOverridesEverything() {
        ArmyMission.Snapshot in = base();
        in.posture = ClusterMission.Posture.ATTACK;
        in.inContact = true;
        in.breached = true;

        assertEquals(ArmyMission.HOLD_MAIN, ArmyMission.decide(in),
            "raiders in the base: fight there, never march out");
    }

    @Test
    public void defendAtMainStaysHome() {
        assertEquals(ArmyMission.HOLD_MAIN, ArmyMission.decide(base()),
            "small line at the base, no contact: stay");
    }

    @Test
    public void defendAtMainChokeHoldsTheMouth() {
        ArmyMission.Snapshot in = base();
        in.distToMain = 9;
        in.distToMainChoke = 1.5;

        assertEquals(ArmyMission.HOLD_MAIN_CHOKE, ArmyMission.decide(in),
            "line already at the choke mouth holds it");
    }

    @Test
    public void defendAtNaturalChokeHoldsTheMouth() {
        ArmyMission.Snapshot in = base();
        in.distToMain = 15;
        in.distToMainChoke = 30;
        in.distToNaturalChoke = 1.5;

        assertEquals(ArmyMission.HOLD_NATURAL_CHOKE, ArmyMission.decide(in),
            "line already at the natural mouth holds it");
    }

    @Test
    public void defendOffStationBelongsHome() {
        ArmyMission.Snapshot in = base();
        in.distToMain = 20;
        in.distToMainChoke = 25;
        in.distToMiddleChoke = 20;

        assertEquals(ArmyMission.HOLD_MAIN, ArmyMission.decide(in),
            "a defend unit with no station is ordered home, not left out");
    }

    @Test
    public void defendAtMiddleIsOrderedHome() {
        ArmyMission.Snapshot in = base();
        in.distToMain = 30;
        in.distToMiddleChoke = 1;

        assertEquals(ArmyMission.HOLD_MAIN, ArmyMission.decide(in),
            "no forward-defend mission: a defend line at middle belongs home");
    }

    @Test
    public void attackWithContactJoinsTheFight() {
        ArmyMission.Snapshot in = base();
        in.posture = ClusterMission.Posture.ATTACK;
        in.inContact = true;

        assertEquals(ArmyMission.ATTACK_ENEMY_BASE, ArmyMission.decide(in),
            "contact joins the fight");
    }

    @Test
    public void attackWithKnownDestinationMarchesToIt() {
        ArmyMission.Snapshot in = base();
        in.posture = ClusterMission.Posture.ATTACK;
        in.enemyBaseKnown = true;
        in.distToEnemyBase = 60;
        in.distToMiddleChoke = 30;

        assertEquals(ArmyMission.ATTACK_ENEMY_BASE, ArmyMission.decide(in),
            "a known destination owns the march - no separate march mission");
    }

    @Test
    public void attackBlindAtMiddleHoldsForContact() {
        ArmyMission.Snapshot in = base();
        in.posture = ClusterMission.Posture.ATTACK;
        in.enemyBaseKnown = false;
        in.distToMiddleChoke = 1.5;

        assertEquals(ArmyMission.HOLD_MIDDLE, ArmyMission.decide(in),
            "reached staging with nowhere to go: hold and wait");
    }

    @Test
    public void attackBlindFarOutKeepsMarching() {
        ArmyMission.Snapshot in = base();
        in.posture = ClusterMission.Posture.ATTACK;
        in.enemyBaseKnown = false;
        in.distToMiddleChoke = 30;

        assertEquals(ArmyMission.STAGE_MIDDLE, ArmyMission.decide(in),
            "no destination yet: march out and form up");
    }

    @Test
    public void stationBoundaryIsStrict() {
        ArmyMission.Snapshot in = base();
        in.distToMain = 9;
        in.distToMainChoke = 2.1;

        assertEquals(ArmyMission.HOLD_MAIN, ArmyMission.decide(in),
            "2.0 is not inside: the boundary reads strictly less-than");
    }
}
