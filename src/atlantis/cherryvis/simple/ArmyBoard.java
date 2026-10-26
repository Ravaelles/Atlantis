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

    private static String lastTask = null;
    private static String lastFocus = null;
    private static int lastStrength = Integer.MIN_VALUE;
    private static int lastValuesFrame = Integer.MIN_VALUE;

    private ArmyBoard() {
    }

    public static void publish(
        int frame, ClusterMission.Posture posture, FightOrFlight.Tactic tactic, HasPosition anchor
    ) {
        if (!ACherryVis.isEnabled()) return;

        String task = posture.name() + "/" + tactic.name();
        if (!task.equals(lastTask)) {
            lastTask = task;
            ACherryVis_GlobalState.logNewState("Task", task);
        }

        String focus = ClusterTwin.focusName(anchor);
        if (!focus.equals(lastFocus)) {
            lastFocus = focus;
            ACherryVis_GlobalState.logNewState("Focus", focus);
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
        lastTask = null;
        lastFocus = null;
        lastStrength = Integer.MIN_VALUE;
        lastValuesFrame = Integer.MIN_VALUE;
        ACherryVis_GlobalState.all.clear();
        ACherryVis_GlobalValues.clearForTests();
    }
}
