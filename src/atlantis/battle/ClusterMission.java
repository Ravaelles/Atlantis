package atlantis.battle;

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

    public static Mission toMission(Posture posture) {
        return posture == Posture.ATTACK ? Missions.ATTACK : Missions.DEFEND;
    }
}
