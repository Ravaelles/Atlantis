package atlantis.battle.cluster;

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
        if (breached) return Posture.DEFEND;
        return decide(combatUnits);
    }

    public static Mission toMission(Posture posture) {
        return posture == Posture.ATTACK ? Missions.ATTACK : Missions.DEFEND;
    }
}
