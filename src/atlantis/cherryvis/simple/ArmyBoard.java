package atlantis.cherryvis.simple;

import atlantis.cherryvis.ACherryVis;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.twins.ClusterTwin;
import atlantis.information.generic.Army;
import atlantis.map.position.HasPosition;

/**
 * Army decisions for the CherryVis board + game-values graph (NEXT #214):
 * the current task (posture/tactic), the focus point and the relative
 * strength - each board key only on change, the graph sampled on a
 * fixed cadence. One call site (BattleCommander, cluster-level, once
 * per frame); transitions, never per-frame rows.
 */
public final class ArmyBoard {

    /** Graph sampling cadence (frames); the board keys are on-change. */
    public static final int VALUES_EVERY_FRAMES = 31;

    private static String lastMission = null;
    private static String lastTactic = null;
    private static String lastReason = null;
    private static String lastFocus = null;
    private static String lastFocusName = null;
    private static int lastStrength = Integer.MIN_VALUE;
    private static int lastValuesFrame = Integer.MIN_VALUE;

    private ArmyBoard() {
    }

    public static void publish(
        int frame, ClusterMission.Posture posture, FightOrFlight.Tactic tactic, String reason, HasPosition anchor
    ) {
        if (!ACherryVis.isEnabled()) return;

        // Mission and tactic ride separate keys: posture is where the
        // army belongs (ATTACK = enemy base, DEFEND = home), tactic is
        // what it does (FIGHT/FLEE) - one combined "ATTACK/FLEE" string
        // reads as a contradiction and it is two answers, not one.
        String mission = posture.name();
        if (!mission.equals(lastMission)) {
            lastMission = mission;
            ACherryVis_GlobalState.logNewState("Mission", mission);
        }

        String tacticName = tactic.name();
        if (!tacticName.equals(lastTactic)) {
            lastTactic = tacticName;
            ACherryVis_GlobalState.logNewState("Tactic", tacticName);
        }

        if (!reason.equals(lastReason)) {
            lastReason = reason;
            ACherryVis_GlobalState.logNewState("Reason", reason);
        }

        String focus = ClusterTwin.focusName(anchor);
        if (!focus.equals(lastFocus)) {
            lastFocus = focus;
            ACherryVis_GlobalState.logNewState("Focus", focus);
        }

        String focusName = ClusterTwin.focusShortName(anchor);
        if (!focusName.equals(lastFocusName)) {
            lastFocusName = focusName;
            ACherryVis_GlobalState.logNewState("FocusName", focusName);
        }

        int strength;
        try {
            strength = Army.strength();
        } catch (Exception ignored) {
            strength = -1;
        }
        if (strength != lastStrength) {
            lastStrength = strength;
            ACherryVis_GlobalState.logNewState("Strength", String.valueOf(strength));
        }

        if (lastValuesFrame == Integer.MIN_VALUE
            || frame - lastValuesFrame >= VALUES_EVERY_FRAMES) {
            lastValuesFrame = frame;
            ACherryVis_GlobalValues.sample(frame, "Strength", strength);
        }
    }

    /** Test seam: the last-value cache is per-game state, never cross-test. */
    public static void clearForTests() {
        lastMission = null;
        lastTactic = null;
        lastReason = null;
        lastFocus = null;
        lastFocusName = null;
        lastStrength = Integer.MIN_VALUE;
        lastValuesFrame = Integer.MIN_VALUE;
        ACherryVis_GlobalState.all.clear();
        ACherryVis_GlobalValues.clearForTests();
    }
}
