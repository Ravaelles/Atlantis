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
