package atlantis.combat.cluster;
import atlantis.battle.cluster.Mission;
import atlantis.battle.cluster.Missions;

import atlantis.combat.focus.PostureLatch;
import atlantis.combat.focus.PostureReasons;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.Count;

/**
 * Where the one cluster belongs: home while small, at the enemy once it
 * can fight. Pure posture - the fight/flee inside the posture belongs to
 * {@link FightOrFlight}. Phase 0 of __PLAN-COMBAT step 5.
 */
public class ClusterMission {

    /** Army size at which the posture flips from DEFEND to ATTACK. */
    public static final int ATTACK_ARMY_SIZE = 8;

    public enum Posture {
        DEFEND,
        ATTACK
    }

    public static Posture decide(int combatUnits) {
        return combatUnits >= ATTACK_ARMY_SIZE ? Posture.ATTACK : Posture.DEFEND;
    }

    /**
     * Posture with the breach override (owner): while raiders are in the
     * base the cluster holds home even when it is big enough to march -
     * marching out abandons the workers to the push. Pure: the breach
     * reading happens at the call site. Public as a seam.
     */
    public static Posture postureFor(int combatUnits, boolean breached) {
        // The reasons read live game state; without a game (world-free
        // pins) fall back to the standing headcount rule, never crash.
        try {
            if (breached) return PostureLatch.hold(Posture.DEFEND, combatUnits, A.now());

            PostureReasons.Verdict verdict = PostureReasons.decide(
                combatUnits,
                Count.dragoons(),
                EnemyUnits.zealots(),
                A.minerals(),
                A.s);

            return PostureLatch.hold(verdict.posture, combatUnits, A.now());
        }
        catch (RuntimeException ignored) {
            return decide(combatUnits);
        }
    }

    /** Test seam: the latch is per-game state, never cross-test. */
    public static void resetForTests() {
        PostureLatch.resetForTests();
    }

    public static Mission toMission(Posture posture) {
        return posture == Posture.ATTACK ? Missions.ATTACK : Missions.DEFEND;
    }
}
