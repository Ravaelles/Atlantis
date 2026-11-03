package tests.unit;

import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.MissionTransitionGate;
import atlantis.combat.cluster.MissionTransitionGate.Verdict;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static atlantis.combat.cluster.ArmyMission.ATTACK_ENEMY_BASE;
import static atlantis.combat.cluster.ArmyMission.HOLD_MAIN;
import static atlantis.combat.cluster.ArmyMission.HOLD_MAIN_CHOKE;
import static atlantis.combat.cluster.ArmyMission.HOLD_MIDDLE;
import static atlantis.combat.cluster.ArmyMission.STAGE_MIDDLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner-ordered mediator for defend&lt;-&gt;attack mission changes: 5 s
 * of hold-fire after a withdrawal, withdrawals re-firing every 2 s.
 * Pure table pins (snapshot in, verdict out - the AttackDeciderTest
 * shape); the wiring is three lines in the shadow sampler and stays
 * green through the existing shadow tests.
 */
@Tag("combat")
public class MissionTransitionGateTest {

    private static final int NEVER = Integer.MIN_VALUE / 2;

    @AfterEach
    public void clean() {
        MissionTransitionGate.clearForTests();
    }

    @Test
    public void undecidedAndSameMissionAlwaysPass() {
        assertEquals(Verdict.ALLOW,
            MissionTransitionGate.decide(null, ATTACK_ENEMY_BASE, 1000, NEVER));
        assertEquals(Verdict.ALLOW,
            MissionTransitionGate.decide(HOLD_MAIN, HOLD_MAIN, 1000, 900));
        assertEquals(Verdict.ALLOW,
            MissionTransitionGate.decide(ATTACK_ENEMY_BASE, ATTACK_ENEMY_BASE, 1000, 900));
    }

    @Test
    public void defendToDefendAlwaysPasses() {
        assertEquals(Verdict.ALLOW,
            MissionTransitionGate.decide(HOLD_MAIN, HOLD_MAIN_CHOKE, 1005, 1000),
            "re-dressing inside the defence is not a transition this gate owns");
        assertEquals(Verdict.ALLOW,
            MissionTransitionGate.decide(HOLD_MAIN_CHOKE, HOLD_MIDDLE, 1005, 1000),
            "this gate owns only the defend<->attack axis");
    }

    @Test
    public void withdrawNeedsTwoSecondsSinceThePreviousOne() {
        assertEquals(Verdict.ALLOW,
            MissionTransitionGate.decide(ATTACK_ENEMY_BASE, HOLD_MAIN, 1000, NEVER),
            "the first withdrawal always sticks");
        assertEquals(Verdict.WITHDRAW_TOO_SOON,
            MissionTransitionGate.decide(ATTACK_ENEMY_BASE, HOLD_MAIN, 1030, 1000),
            "30 frames after a withdrawal the next one waits");
        assertEquals(Verdict.ALLOW,
            MissionTransitionGate.decide(ATTACK_ENEMY_BASE, HOLD_MAIN, 1065, 1000),
            "65 frames later it fires");
    }

    @Test
    public void attackHoldsFireForFiveSecondsAfterWithdraw() {
        assertEquals(Verdict.HOLD_FIRE_POST_WITHDRAW,
            MissionTransitionGate.decide(HOLD_MAIN, ATTACK_ENEMY_BASE, 1100, 1000),
            "100 frames after the withdrawal the attack waits");
        assertEquals(Verdict.ALLOW,
            MissionTransitionGate.decide(HOLD_MAIN, ATTACK_ENEMY_BASE, 1160, 1000),
            "160 frames later it marches");
    }

    @Test
    public void stagingCountsDefendWard() {
        assertEquals(Verdict.ALLOW,
            MissionTransitionGate.decide(ATTACK_ENEMY_BASE, STAGE_MIDDLE, 2000, NEVER),
            "falling back to the middle is a withdrawal");
        assertEquals(Verdict.HOLD_FIRE_POST_WITHDRAW,
            MissionTransitionGate.decide(STAGE_MIDDLE, ATTACK_ENEMY_BASE, 2050, 2000),
            "pushing out of the middle honours the hold-fire");
    }

    @Test
    public void allowsStampsOnlyAllowedWithdrawals() {
        assertTrue(MissionTransitionGate.allows(ATTACK_ENEMY_BASE, HOLD_MAIN, 1000),
            "first withdrawal sticks and stamps");
        assertFalse(MissionTransitionGate.allows(ATTACK_ENEMY_BASE, HOLD_MAIN, 1030),
            "30 frames later the flap is vetoed and stamps nothing");
        assertTrue(MissionTransitionGate.allows(ATTACK_ENEMY_BASE, HOLD_MAIN, 1070),
            "65 frames later it fires and re-stamps");
        assertFalse(MissionTransitionGate.allows(HOLD_MAIN, ATTACK_ENEMY_BASE, 1100),
            "the re-stamp at 1070 holds the attack: 30 frames is not 150");
        assertTrue(MissionTransitionGate.allows(HOLD_MAIN, ATTACK_ENEMY_BASE, 1230),
            "160 frames after the last withdrawal the attack marches");
    }

    @Test
    public void reasonNamesNumbers() {
        assertTrue(MissionTransitionGate.reason(
            Verdict.HOLD_FIRE_POST_WITHDRAW, HOLD_MAIN, ATTACK_ENEMY_BASE, 1100, 1000)
            .contains("100"),
            "the reason carries the deciding numbers: "
                + MissionTransitionGate.reason(
                    Verdict.HOLD_FIRE_POST_WITHDRAW, HOLD_MAIN, ATTACK_ENEMY_BASE, 1100, 1000));
    }
}
