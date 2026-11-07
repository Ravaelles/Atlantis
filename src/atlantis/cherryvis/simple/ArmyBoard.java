package atlantis.cherryvis.simple;

import atlantis.cherryvis.ACherryVis;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.twins.ClusterTwin;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.map.position.HasPosition;
import cherryvis.GameTrace;

/**
 * Army decisions for the CherryVis board + game-values graph (NEXT #214):
 * the current task (posture/tactic), the focus point and the relative
 * strength - each board key only on change, the graph sampled on a
 * fixed cadence. One call site (BattleCommander, cluster-level, once
 * per frame); transitions, never per-frame rows.
 */
public final class ArmyBoard {

    /**
     * Graph sampling cadence (frames); the board keys are on-change.
     */
    public static final int VALUES_EVERY_FRAMES = 31;

    private static String lastAlphaMission = null;
    private static String lastAlphaTactic = null;
    private static String lastAlphaReason = null;
    private static String lastAlphaFocus = null;
    private static String lastAlphaFocusName = null;
    private static String lastAlphaBan = null;
    private static int lastAlphaStrength = Integer.MIN_VALUE;
    private static int lastValuesFrame = Integer.MIN_VALUE;

    private ArmyBoard() {
    }

    public static void publish(
        int frame, ClusterMission.Posture posture, FightOrFlight.Tactic tactic, String reason, HasPosition anchor
    ) {
        if (!ACherryVis.isEnabled()) return;

        GameTrace trace = ACherryVis.trace();

        // Mission and tactic ride separate keys: posture is where the
        // army belongs (ATTACK = enemy base, DEFEND = home), tactic is
        // what it does (FIGHT/FLEE) - one combined "ATTACK/FLEE" string
        // reads as a contradiction and it is two answers, not one.
        String mission = posture.name();
        if (!mission.equals(lastAlphaMission)) {
            lastAlphaMission = mission;
            trace.logState(frame, "AlphaMission", mission);
        }

        String tacticName = tactic.name();
        if (!tacticName.equals(lastAlphaTactic)) {
            lastAlphaTactic = tacticName;
            trace.logState(frame, "AlphaTactic", tacticName);
        }

        if (!reason.equals(lastAlphaReason)) {
            lastAlphaReason = reason;
            trace.logState(frame, "AlphaReason", reason);
        }

        String focus = ClusterTwin.focusName(anchor);
        if (!focus.equals(lastAlphaFocus)) {
            lastAlphaFocus = focus;
            trace.logState(frame, "AlphaFocus", focus);
        }

        String focusName = ClusterTwin.focusShortName(anchor);
        if (!focusName.equals(lastAlphaFocusName)) {
            lastAlphaFocusName = focusName;
            trace.logState(frame, "AlphaFocusName", focusName);
        }

        String ban = banState();
        if (!ban.equals(lastAlphaBan)) {
            lastAlphaBan = ban;
            trace.logState(frame, "AlphaBan", ban);
        }

        int strength;
        try {
            strength = Army.strength();
        } catch (Exception ignored) {
            strength = -1;
        }
        if (strength != lastAlphaStrength) {
            lastAlphaStrength = strength;
            trace.logState(frame, "AlphaStrength", String.valueOf(strength));
        }

        if (lastValuesFrame == Integer.MIN_VALUE
            || frame - lastValuesFrame >= VALUES_EVERY_FRAMES) {
            lastValuesFrame = frame;
            trace.sampleValue(frame, "Strength", strength);
        }
    }

    /**
     * The main-choke ban, named with its headcount (NEXT #194): a Protoss
     * dragoon mass over the ban never stages home, and neither does a
     * 3+ base game. "none" when no ban stands, so the row also witnesses
     * the ban lifting.
     */
    private static String banState() {
        try {
            if (atlantis.combat.focus.ProtossDefendFocusPoint.mainChokeBannedByDragoons()) {
                return "main-choke:" + atlantis.units.select.Count.dragoons() + "-dragoons";
            }
            if (atlantis.combat.focus.ProtossDefendFocusPoint.mainChokeBannedByBases()) {
                return "main-choke:"
                    + atlantis.units.select.Select.ourWithUnfinished().bases().count() + "-bases";
            }
        } catch (RuntimeException ignored) {
        }

        return "none";
    }

    /**
     * Test seam: the last-value cache is per-game state, never cross-test.
     */
    public static void clearForTests() {
        lastAlphaMission = null;
        lastAlphaTactic = null;
        lastAlphaReason = null;
        lastAlphaFocus = null;
        lastAlphaFocusName = null;
        lastAlphaBan = null;
        lastAlphaStrength = Integer.MIN_VALUE;
        lastValuesFrame = Integer.MIN_VALUE;
        lastDefendedPlace = null;
        ACherryVis.trace().clear();
    }

    private static String lastDefendedPlace = null;

    /**
     * Which ground the mission defends (NEXT #194): the DefendedPlace
     * answer kind and tile, change-only like every other board key. The
     * focus keys say WHERE the line looks; this one says WHY there (which
     * ground won: breach, choke, base). Called where the answer is
     * computed, never on the hot path (TTL-memoized supplier).
     */
    public static void noteDefendedPlace(String kind, HasPosition tile) {
        if (!ACherryVis.isEnabled()) return;

        String value = kind + " " + (tile == null ? "-" : "[" + tile.tx() + "," + tile.ty() + "]");
        if (value.equals(lastDefendedPlace)) return;
        lastDefendedPlace = value;
        ACherryVis.trace().logState(A.now(), "DefendedPlace", value);
    }
}
