package atlantis.combat.cluster;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.Behavior;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * No lone melee attacks, systemically (owner order 2026-11-07): a Zealot
 * that acquires a target while detached from the ball does not engage -
 * it rejoins. Ranged units keep their kite/escape doctrine (sim-pinned:
 * pocket-escape, base-raid, duel-kit - a leash over them breaks those
 * pins, so they are a separate slice with their own vehicle). The gate
 * sits in the commander's fight branch, so no behavior, kite or chase
 * can route around it.
 *
 * <p>Rules, in order: in the ball (within {@value #STRAY_TILES} tiles
 * of the median) fights; chewing its target (within a tile) finishes
 * it instead of turning around mid-bite. Everything else links up:
 * even the spearhead holds for the ball instead of duelling the army
 * alone (Stardust formArc waits for the ball the same way). Scouts
 * reveal through contact and fall back - the march out (no target
 * yet) is untouched, only the lone engagement dies.</p>
 */
public final class ClusterCohesion {

    /**
     * Past this many tiles from the ball's median a unit with a target
     * is a straggler, never a spearhead-in-waiting.
     */
    public static final double STRAY_TILES = 6;

    private ClusterCohesion() {
    }

    /**
     * True when the unit was ordered back to the ball instead of
     * fighting: a detached melee unit, not chewing its target.
     */
    public static boolean recallStraggler(AUnit unit, AUnit target) {
        if (unit == null || target == null) return false;
        if (!unit.isZealot()) return false;

        try {
            return recallStragglerInner(unit, target);
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    private static boolean recallStragglerInner(AUnit unit, AUnit target) {
        APosition median = Alpha.get().median();
        if (median == null) return false;

        if (unit.distTo(median) <= STRAY_TILES) return false;
        if (unit.distTo(target) <= 1) return false;

        boolean moved = unit.move(median, Actions.MOVE_FORMATION, Behavior.DRESS.tag());
        if (System.getenv("ATLANTIS_TMPDIAG") != null)
            System.err.println("TMPDIRAG-TRY unit=" + unit.id() + " moved=" + moved);
        if (moved) {
            atlantis.combat.twins.CohesionTwin.recalled(unit, target, median);

            return true;
        }

        return false;
    }
}
