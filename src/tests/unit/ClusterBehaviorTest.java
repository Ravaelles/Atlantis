package tests.unit;

import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.FightOrFlight;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * __PLAN-COMBAT step 5, Phase 0: one posture for the whole cluster
 * (home while small, at the enemy when it can fight) and one gate
 * (evaluator fight/flee with a retreat latch). Both clock-free and
 * engine-free - the commander only passes frames in.
 */
@Tag("combat")
public class ClusterBehaviorTest {

    @Test
    public void smallArmyDefendsBigArmyAttacks() {
        assertEquals(
            ClusterMission.Posture.DEFEND, ClusterMission.decide(0));
        assertEquals(
            ClusterMission.Posture.DEFEND,
            ClusterMission.decide(ClusterMission.ATTACK_ARMY_SIZE - 1));
        assertEquals(
            ClusterMission.Posture.ATTACK,
            ClusterMission.decide(ClusterMission.ATTACK_ARMY_SIZE));
    }

    @Test
    public void evalDecidesFightOrFlee() {
        assertEquals(
            FightOrFlight.Tactic.FIGHT, FightOrFlight.decide(1.0, 1000, -1));
        assertEquals(
            FightOrFlight.Tactic.FIGHT, FightOrFlight.decide(5.0, 1000, -1));
        assertEquals(
            FightOrFlight.Tactic.FLEE, FightOrFlight.decide(0.9, 1000, -1));
    }

    @Test
    public void retreatLatchHoldsTenSeconds() {
        // Flee starts at frame 1000: still fleeing at +100 and +239...
        assertEquals(
            FightOrFlight.Tactic.FLEE, FightOrFlight.decide(99.0, 1100, 1000),
            "a won eval mid-retreat must not turn the cluster");
        assertEquals(
            FightOrFlight.Tactic.FLEE, FightOrFlight.decide(99.0, 1239, 1000));

        // ...free at +240 even losing (re-evaluation resumes).
        assertEquals(
            FightOrFlight.Tactic.FLEE, FightOrFlight.decide(0.5, 1240, 1000));
        assertEquals(
            FightOrFlight.Tactic.FIGHT, FightOrFlight.decide(2.0, 1240, 1000));
    }

    @Test
    public void noRetreatMeansNoLatch() {
        assertEquals(
            FightOrFlight.Tactic.FIGHT, FightOrFlight.decide(3.0, 5000, -1));
    }
}
