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
 *
 * <p>Stability (Stardust consecutive-frames doctrine): the first recall
 * latches the unit onto rejoining until it reaches the ball, takes a
 * hit, meets contact, or the latch times out. Without the latch the
 * frame after a recall re-acquires and the line flaps hold/recall
 * forever (measured live: one Zealot, 5000+ frames, never rejoined
 * and never committed). Pain and contact break the march, like damage
 * breaks the hold.</p>
 */
public final class ClusterCohesion {

    /**
     * Past this many tiles from the ball's median a unit with a target
     * is a straggler, never a spearhead-in-waiting.
     */
    public static final double STRAY_TILES = 6;

    /**
     * A latched rejoin that never completes is abandoned, not marched
     * forever: the ball moved on, the tile never got walkable, the war
     * moved elsewhere. The next contact re-latches from scratch.
     */
    private static final int LATCH_TIMEOUT_FRAMES = 900;

    private static final java.util.Map<Integer, Integer> latchedSince = new java.util.HashMap<>();

    private ClusterCohesion() {
    }

    public static void clearForTests() {
        latchedSince.clear();
    }

    /**
     * True when the unit was ordered back to the ball instead of
     * fighting: a detached melee unit, not chewing its target.
     */
    public static boolean recallStraggler(AUnit unit, AUnit target) {
        if (unit == null) return false;
        if (!unit.isZealot()) return false;

        try {
            return recallStragglerInner(unit, target);
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    private static boolean recallStragglerInner(AUnit unit, AUnit target) {
        APosition median = Alpha.get().median();
        if (median == null) {
            latchedSince.remove(unit.id());
            return false;
        }

        Integer latchedAt = latchedSince.get(unit.id());
        if (latchedAt != null) {
            if (unit.distTo(median) <= STRAY_TILES) {
                latchedSince.remove(unit.id());
                return false;
            }
            if (atlantis.game.A.now() - latchedAt > LATCH_TIMEOUT_FRAMES) {
                latchedSince.remove(unit.id());
                return false;
            }
            if (threatAdjacent(unit) || atlantis.battle.DuelMemory.tookDamageSinceLastBattleFrame(unit)) {
                latchedSince.remove(unit.id());
                return false;
            }

            return moveBack(unit, median, target);
        }

        if (target == null) return false;
        if (unit.distTo(median) <= STRAY_TILES) return false;
        if (unit.distTo(target) <= 1) return false;

        if (latchedSince.size() > 64) latchedSince.clear();
        latchedSince.put(unit.id(), atlantis.game.A.now());

        return moveBack(unit, median, target);
    }

    private static boolean moveBack(AUnit unit, APosition median, AUnit target) {
        if (unit.move(median, Actions.MOVE_FORMATION, Behavior.DRESS.tag())) {
            atlantis.combat.twins.CohesionTwin.recalled(unit, target, median);

            return true;
        }

        return false;
    }

    private static boolean threatAdjacent(AUnit unit) {
        for (AUnit enemy : atlantis.units.select.Select.enemy().list()) {
            if (enemy == null || !enemy.isAlive() || !enemy.hasPosition()) continue;
            if (!enemy.isVisibleUnitOnMap()) continue;
            if (unit.distTo(enemy) <= 1.5) return true;
        }

        return false;
    }
}
