package atlantis.battle.escape;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;

import java.util.List;

/**
 * Destination scoring for escape candidates. Every term is a pure
 * function of positions, so duels, chokes and mineral lines pin without
 * a map. Tunable weights live here, one constant per term (NEXT #172:
 * the batch tuner optimizes them against win-then-hits-then-seconds).
 */
public final class EscapeScoring {

    private EscapeScoring() {
    }

    /** Blockers inside this radius of a destination count against it. */
    public static final double STATIC_RADIUS_TILES = 3;

    /** Weight of one close blocker, in the same arbitrary units as all terms. */
    public static final double STATIC_WEIGHT = 10;

    /** Rays shorter than this count as blocked (pocket walls, not fields). */
    public static final double BLOCKED_RAY_TILES = 3;

    /** Weight per blocked ray. */
    public static final double CORNER_WEIGHT = 8;

    /** Extra penalty when the single most open direction is still short. */
    public static final double POCKET_EXTRA = 20;

    /** Enemies this far from the destination count against it. */
    public static final double ENEMY_RADIUS_TILES = 20;

    /** Weight of enemy pressure, same units as the other terms. */
    public static final double ENEMY_WEIGHT = 5;

    /**
     * Corner/peninsula penalty from the 8-dir clearance at the
     * destination: count blocked rays (walls on several sides mean the
     * step walks into a pocket), plus extra when even the best ray is
     * short (a peninsula with no way out scores worst).
     */
    public static double cornerPenalty(double[] clearance) {
        if (clearance == null || clearance.length == 0) return 0;
        int blocked = 0;
        double best = 0;
        for (double c : clearance) {
            if (c < BLOCKED_RAY_TILES) blocked++;
            if (c > best) best = c;
        }
        double penalty = blocked * CORNER_WEIGHT;
        if (best < BLOCKED_RAY_TILES * 2) penalty += POCKET_EXTRA;
        return penalty;
    }

    /**
     * Enemy avoidance: every known enemy within 20 tiles of the
     * destination counts, closer ones more (radius - dist). The point
     * is the second Zealot, not the nearest one - a pocket with two
     * melee inside is a death sentence whatever the nearest does.
     */
    public static double enemyPressure(
        APosition dest, List<? extends HasPosition> enemies, double radiusTiles
    ) {
        if (dest == null || enemies == null || enemies.isEmpty() || radiusTiles <= 0) return 0;
        double pressure = 0;
        for (HasPosition enemy : enemies) {
            if (enemy == null) continue;
            double dist = dest.distTo(enemy);
            if (dist < radiusTiles) pressure += (radiusTiles - dist);
        }
        return pressure * ENEMY_WEIGHT;
    }

    /**
     * Static-obstacle penalty: minerals, geysers and buildings near the
     * destination (a mineral line behind you is a wall you cannot step
     * through). Closer blockers weigh more: sum of (radius - dist).
     */
    public static double staticPenalty(
        APosition dest, List<? extends HasPosition> blockers, double radiusTiles
    ) {
        if (dest == null || blockers == null || blockers.isEmpty() || radiusTiles <= 0) return 0;
        double penalty = 0;
        for (HasPosition blocker : blockers) {
            if (blocker == null) continue;
            double dist = dest.distTo(blocker);
            if (dist < radiusTiles) penalty += (radiusTiles - dist);
        }
        return penalty * STATIC_WEIGHT;
    }
}
