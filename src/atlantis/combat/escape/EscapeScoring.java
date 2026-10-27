package atlantis.combat.escape;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;

import java.util.List;

/**
 * Destination scoring for escape candidates. Every term is a pure
 * function of positions, so duels, chokes and mineral lines pin without
 * a map. Tunable weights live in {@code EscapeTuning} (NEXT #172 step 5:
 * the batch tuner optimizes them against win-then-hits-then-seconds);
 * this class only applies them.
 */
public final class EscapeScoring {

    /**
     * Rays shorter than this count as blocked (pocket walls, not fields).
     */
    public static final double BLOCKED_RAY_TILES = 3;

    /**
     * Tunable weights live in {@code EscapeTuning} (NEXT #172 step 5:
     * the batch tuner optimizes them); this class only applies them.
     * Radii stay explicit parameters (callers read the tuning holder);
     * {@code BLOCKED_RAY_TILES} stays a constant: it defines what a
     * pocket wall is, not what it costs.
     */

    private EscapeScoring() {
    }

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
        double penalty = blocked * EscapeTuning.current().getCornerWeight();
        if (best < BLOCKED_RAY_TILES * 2) penalty += EscapeTuning.current().getPocketExtra();

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
        return pressure * EscapeTuning.current().getEnemyWeight();
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
        return penalty * EscapeTuning.current().getStaticWeight();
    }
}
