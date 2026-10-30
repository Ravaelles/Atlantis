package atlantis.combat.cluster;

import atlantis.map.position.Proximity;

/**
 * One named job for the whole cluster (owner 2026-10-30, priority 0).
 * Where {@code Behavior} is per-unit and per-frame, a mission is the
 * cluster's standing order: what we are doing here, in words.
 *
 * <p>Shadow stage (slice 1a): {@link #decide} is pure - snapshot in,
 * mission out - and nothing reads it yet. It proves the vocabulary
 * covers reality: every early-game behavior pins as input-output rows
 * without a game. Enforcement (slice 3+) wires one mission at a time,
 * with game proof each; until then posture, tactic and strategy keep
 * deciding on their own.</p>
 *
 * <p>Layering note: {@code decide} takes the posture as an input. That
 * is the mapping direction for the shadow only - the end state reads
 * the mission first and derives the posture (MISSION-DRIVEN-COMMAND.md
 * slice 5). Do not build new readers on this order.</p>
 */
public enum ArmyMission {

    /** Stay at the main base and defend it. */
    HOLD_MAIN,
    /** Hold the main choke standoff, never exit. */
    HOLD_MAIN_CHOKE,
    /** Hold the natural choke standoff, never exit. */
    HOLD_NATURAL_CHOKE,
    /** March out and form up; destination unknown. */
    STAGE_MIDDLE,
    /** Reached the middle staging point; hold and wait for contact. */
    HOLD_MIDDLE,
    /** The enemy base is the destination: fight there or march to it. */
    ATTACK_ENEMY_BASE;

    /**
     * The mission's inputs, all plain numbers and flags - no live
     * reads inside {@code decide}, so pins name the whole contract.
     * Distances in tiles; unknown reads {@link Double#MAX_VALUE}.
     */
    public static final class Snapshot {

        /** The standing posture (mass/headcount doctrine, unchanged). */
        public ClusterMission.Posture posture = ClusterMission.Posture.DEFEND;
        /** Visible enemy contact right now. */
        public boolean inContact;
        /** Raiders in the base: everything else yields. */
        public boolean breached;
        public double distToMain = Double.MAX_VALUE;
        public double distToMainChoke = Double.MAX_VALUE;
        public double distToNaturalChoke = Double.MAX_VALUE;
        public double distToMiddleChoke = Double.MAX_VALUE;
        public boolean enemyBaseKnown;
        public double distToEnemyBase = Double.MAX_VALUE;
    }

    /**
     * One job for the snapshot, first rule wins. Breach first (fight in
     * the base, never march out of it); then the posture's station
     * rules; a defend unit with no station belongs home.
     */
    public static ArmyMission decide(Snapshot in) {
        if (in.breached) return HOLD_MAIN;

        if (in.posture == ClusterMission.Posture.ATTACK) return attackMission(in);

        if (in.distToMainChoke < Proximity.AT_STATION.tiles()) return HOLD_MAIN_CHOKE;
        if (in.distToNaturalChoke < Proximity.AT_STATION.tiles()) return HOLD_NATURAL_CHOKE;

        return HOLD_MAIN;
    }

    private static ArmyMission attackMission(Snapshot in) {
        // Joined or about to join: contact, or a known destination.
        // The march to a known base IS the attack - no separate
        // march mission while the destination is known.
        if (in.inContact) return ATTACK_ENEMY_BASE;
        if (in.enemyBaseKnown) return ATTACK_ENEMY_BASE;

        // Unknown destination: march out and form up; once at the
        // middle staging point, hold and wait for contact.
        if (in.distToMiddleChoke < Proximity.AT_STATION.tiles()) return HOLD_MIDDLE;

        return STAGE_MIDDLE;
    }
}
