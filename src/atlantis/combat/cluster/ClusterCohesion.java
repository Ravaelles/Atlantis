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
     * The latch releases here, inside the engage radius: engaging at 6
     * and releasing at 6 parks the unit on the boundary, marching out
     * and getting recalled every few seconds. The 2-tile deadband keeps
     * the ball a ball instead of a breathing rim.
     */
    static final double REJOIN_TILES = 4;

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

    /**
     * The march half of the leash: a detached melee unit WITHOUT a
     * target rejoins the ball instead of marching alone to the anchor,
     * slot or ghost the march chain would otherwise hand it (measured
     * live: one Zealot flapping GATHER/COLLAPSE/DRESS/STANDOFF for 760
     * frames, never in the ball). Same latch, same radii as the contact
     * half above, so a rejoining unit cannot flap between the two.
     *
     * <p>Exempt by design, not by distance: the vanguard scout runs ahead
     * (it owns first contact), and a unit creeping a remembered contact
     * is on a mission, not lost. Ranged units keep their kite doctrine
     * (separate slice) - this stays melee-only like the contact half.</p>
     */
    public static boolean recallMarchStraggler(AUnit unit) {
        if (unit == null) return false;
        if (!unit.isZealot() || !unit.isAlive()) return false;

        try {
            if (atlantis.combat.strategy.GhostAdvance.isVanguard(unit)) return false;
            if (atlantis.combat.strategy.GhostAdvance.effectiveGhostPointFor(unit) != null) return false;

            return recallLatched(unit);
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    private static boolean recallStragglerInner(AUnit unit, AUnit target) {
        APosition median = medianOrUnlatch(unit);
        if (median == null) return false;

        Integer latchedAt = latchedSince.get(unit.id());
        if (latchedAt != null) return holdLatch(unit, median, latchedAt, target);
        if (target == null) return false;
        if (unit.distTo(median) <= STRAY_TILES) return false;
        if (unit.distTo(target) <= 1) return false;

        latch(unit);

        return moveBack(unit, median, target);
    }

    private static boolean recallLatched(AUnit unit) {
        APosition median = medianOrUnlatch(unit);
        if (median == null) return false;

        Integer latchedAt = latchedSince.get(unit.id());
        if (latchedAt != null) return holdLatch(unit, median, latchedAt, null);
        if (unit.distTo(median) <= STRAY_TILES) return false;

        latch(unit);

        return moveBack(unit, median, null);
    }

    private static APosition medianOrUnlatch(AUnit unit) {
        APosition median = Alpha.get().median();
        if (median == null) {
            latchedSince.remove(unit.id());
        }

        return median;
    }

    private static boolean holdLatch(AUnit unit, APosition median, int latchedAt, AUnit target) {
        if (unit.distTo(median) <= REJOIN_TILES) {
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

    private static void latch(AUnit unit) {
        if (latchedSince.size() > 64) latchedSince.clear();
        latchedSince.put(unit.id(), atlantis.game.A.now());
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
