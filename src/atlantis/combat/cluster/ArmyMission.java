package atlantis.combat.cluster;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.ArmyContact;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Proximity;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

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
     * Shadow sampling (slice 1b): build the snapshot from live state,
     * decide, twin the answer. Called from the cluster handle every
     * frame; it never steers anything - enforcement is a later slice
     * with its own game proof. Skips silently when the cluster has no
     * readable center.
     */
    public static void sampleShadow(ClusterMission.Posture posture) {
        try {
            Snapshot in = readLive(posture);
            if (in == null) return;

            ArmyMission mission = decide(in);
            current = mission;
            ShadowTwin.row(mission, in);
        } catch (Exception | Error ignored) {
        }
    }

    public static void clearForTests() {
        current = null;
        ShadowTwin.clear();
    }

    /**
     * The mission decided this frame (slice 3a): enforcement reads what
     * the shadow decided, never the other way around. Null before the
     * first sample of the frame.
     */
    public static ArmyMission current() {
        return current;
    }

    private static ArmyMission current;

    /**
     * Slice 3a, first enforced mission: HOLD_MAIN_CHOKE forbids the
     * out-march (collapse-to-anchor march, ghost creep past the choke -
     * the mission's own forbidden-branches table). Every other mission
     * leaves the branches alone.
     */
    public static boolean forbidsOutMarch() {
        return current == HOLD_MAIN_CHOKE;
    }

    /**
     * The shadow's twin (slice 1b): what the shadow decider answered,
     * with the inputs that decided it. Change-only with a heartbeat:
     * transitions always row, steady states remind every 300 frames.
     */
    static final class ShadowTwin {

        private static ArmyMission lastTwinned;
        private static int lastTwinFrame = -1000000;

        private ShadowTwin() {
        }

        static void row(ArmyMission mission, Snapshot in) {
            try {
                int now = A.now();
                boolean transition = lastTwinned != mission;
                if (!transition && now - lastTwinFrame < 300) return;

                lastTwinned = mission;
                lastTwinFrame = now;
                EventLog.key(LogKey.MISSION_SHADOW)
                    .param("mission", mission.name())
                    .param("posture", in.posture == null ? "-" : in.posture.name())
                    .param("breach", in.breached)
                    .param("contact", in.inContact)
                    .param("dMain", (int) in.distToMain)
                    .param("dChoke", (int) in.distToMainChoke)
                    .param("transition", transition)
                    .log(now, A.minSec());
            } catch (Exception ignored) {
            }
        }

        static void clear() {
            lastTwinned = null;
            lastTwinFrame = -1000000;
        }
    }

    private static Snapshot readLive(ClusterMission.Posture posture) {
        HasPosition center = Alpha.get().center();
        if (center == null) return null;

        Snapshot in = new Snapshot();
        in.posture = posture;
        in.inContact = ArmyContact.isOngoingFight();
        in.breached = ClusterBreach.breachForcesFight();

        try {
            AUnit main = HomeGround.resolve().main();
            if (main != null && main.position() != null) {
                in.distToMain = center.distTo(main);
            }
        } catch (Exception | Error ignored) {
        }

        try {
            atlantis.map.choke.AChoke choke = HomeGround.resolve().mainChoke();
            if (choke != null && choke.center() != null) {
                in.distToMainChoke = center.distTo(choke.center());
            }
        } catch (Exception | Error ignored) {
        }

        try {
            atlantis.map.choke.AChoke natural = atlantis.map.choke.Chokes.natural();
            if (natural != null && natural.center() != null) {
                in.distToNaturalChoke = center.distTo(natural.center());
            }
        } catch (Exception | Error ignored) {
        }

        try {
            atlantis.map.choke.AChoke middle = atlantis.combat.focus.MiddleFocusChoke.get();
            if (middle != null && middle.center() != null) {
                in.distToMiddleChoke = center.distTo(middle.center());
            }
        } catch (Exception | Error ignored) {
        }

        try {
            APosition foeBase = atlantis.map.base.define.EnemyMainBase.get();
            if (foeBase != null) {
                in.enemyBaseKnown = true;
                in.distToEnemyBase = center.distTo(foeBase);
            }
        } catch (Exception | Error ignored) {
        }

        return in;
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
