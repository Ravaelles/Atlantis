package atlantis.battle.escape;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;

/**
 * Walkable clearance in 8 compass directions from a point, Stardust-style
 * ray marching (furthest walkable wins): step tile by tile out to the max
 * range, the first unwalkable tile stops that ray. The caller supplies
 * walkability, so this stays pure and unit-testable without a map.
 */
public final class EscapeRays {

    private EscapeRays() {
    }

    public interface Walkability {
        boolean isWalkable(int tx, int ty);
    }

    /** 8 compass deltas: E, SE, S, SW, W, NW, N, NE. */
    public static final int[][] DIRS = {
        { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 },
        { -1, 0 }, { -1, -1 }, { 0, -1 }, { 1, -1 },
    };

    /**
     * Index into {@link #DIRS} closest to the away-vector (from threat
     * through us): the straight line out. Ties keep the first (E-first)
     * direction; a zero vector reads as east. Pure geometry, no map.
     */
    public static int awayDirIndex(HasPosition from, HasPosition threat) {
        if (from == null || threat == null) return 0;

        double dx = from.x() - threat.x();
        double dy = from.y() - threat.y();
        if (dx == 0 && dy == 0) return 0;

        double len = Math.sqrt(dx * dx + dy * dy);
        double nx = dx / len;
        double ny = dy / len;
        // Cosine against unit compass vectors (diagonals normalized -
        // raw deltas would tie a diagonal with its straight neighbour
        // on axis-aligned threats). Highest cosine wins, first on ties.
        int best = 0;
        double bestDot = Double.NEGATIVE_INFINITY;
        for (int d = 0; d < DIRS.length; d++) {
            double dirLen = Math.sqrt(DIRS[d][0] * DIRS[d][0] + DIRS[d][1] * DIRS[d][1]);
            double dot = (DIRS[d][0] * nx + DIRS[d][1] * ny) / dirLen;
            if (dot > bestDot) {
                bestDot = dot;
                best = d;
            }
        }
        return best;
    }

    /** Corridor a large unit (Dragoon body 31px) still turns in. */
    public static final int WIDE_CORRIDOR_TILES = 2;

    /**
     * Whether a ray of this clearance fits the unit: small and medium
     * slip through single tiles, large units need a two-tile corridor
     * (their body plus turn room - measured: Dragoons die in gaps
     * Zealots walk through).
     */
    public static boolean passable(double clearTiles, boolean needsWide) {
        return clearTiles >= (needsWide ? WIDE_CORRIDOR_TILES : 1);
    }

    /**
     * Walkable tiles per direction, capped at maxTiles. A ray blocked
     * immediately reads 0; open ground reads maxTiles on all eight.
     */
    public static double[] clearance(APosition from, int maxTiles, Walkability walk) {
        double[] out = new double[DIRS.length];
        if (from == null || walk == null || maxTiles <= 0) return out;

        int fx = from.tx();
        int fy = from.ty();
        for (int d = 0; d < DIRS.length; d++) {
            int clear = 0;
            for (int step = 1; step <= maxTiles; step++) {
                int tx = fx + DIRS[d][0] * step;
                int ty = fy + DIRS[d][1] * step;
                if (!walk.isWalkable(tx, ty)) break;
                clear = step;
            }
            out[d] = clear;
        }
        return out;
    }
}
