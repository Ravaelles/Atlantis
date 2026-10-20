package atlantis.battle.escape;

import atlantis.map.position.APosition;

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
