package jbweb;

import bwapi.*;

public class Pathfinding {
    static PathCache unitPathCache = new PathCache();

    static int maxCacheSize = 10000;

    /** Grid is 256x256 tiles - the engine's maximum, and the size our walkGrid uses. */
    private static final int GRID = 256;

    /// Clears the entire Pathfinding cache. All Paths will be generated as a new Path.
    public static void clearCache() {
        unitPathCache.indexList.clear();
        unitPathCache.pathCacheIndex = 0;
        unitPathCache.pathCache.clear();
        reachable = null;
    }

    /// Returns true if the TilePosition is walkable (does not include any buildings).
    public static boolean terrainWalkable(TilePosition tile) {
        return JBWEB.isWalkable(tile);
    }

    /// Returns true if the TilePosition is walkable (includes buildings).
    public static boolean unitWalkable(TilePosition tile) {
        return JBWEB.isWalkable(tile) && JBWEB.isUsed(tile, 1, 1) == UnitType.None;
    }

    // ===========================================================================
    // Reachability over our own walkGrid
    // ===========================================================================

    /**
     * Cached flood fill from the last source, so repeated "can I get there"
     * questions in one frame are one BFS, not N. Cleared by {@link #clearCache()}
     * (which the unit lifecycle already calls on every building discovered or
     * destroyed, so a new building invalidates it).
     */
    private static boolean[] reachable;
    private static int reachableFromX = -1;
    private static int reachableFromY = -1;

    /**
     * Can a ground unit walk from one tile to another? Answered from
     * {@code JBWEB.walkGrid} with a flood fill - <b>not</b> from the engine's
     * {@code hasPath}.
     *
     * <p>
     * This exists because the engine cannot answer on OpenBW: {@code hasPath} is a
     * region-group comparison there and the region table is empty, so it returns
     * false for every pair, including a tile against itself
     * ({@code _AI/CHALLENGES/OpenBW-API.md}). Our own terrain model is built from
     * tiles and measured healthy ({@code TerrainAnalysis.md}), so the answer comes
     * from it.
     * </p>
     *
     * <p>
     * The shape is the one PurpleWave and this file's own {@code Path.bfsPath}
     * already use: 4-way neighbours over walkable tiles. It is deliberately a
     * flood fill from the source rather than a point-to-point search, because the
     * caller usually asks about several destinations in a frame (the placement
     * candidates) and one fill answers all of them.
     * </p>
     */
    public static boolean reachable(TilePosition from, TilePosition to) {
        if (from == null || to == null) return false;
        if (!inGrid(from) || !inGrid(to)) return false;
        if (from.equals(to)) return JBWEB.isWalkable(from);

        if (reachable == null || from.x != reachableFromX || from.y != reachableFromY) {
            floodFillFrom(from);
            reachableFromX = from.x;
            reachableFromY = from.y;
        }

        return reachable[to.y * GRID + to.x];
    }

    /**
     * Bounds are checked against the grid, not {@code TilePosition.isValid(game)}:
     * this question is about our own model and must answer without a live game
     * (it is asked from tests, and {@code isValid} dereferences a null Game there).
     */
    private static boolean inGrid(TilePosition tile) {
        return tile.x >= 0 && tile.y >= 0 && tile.x < GRID && tile.y < GRID;
    }

    /**
     * Marks every tile a ground unit can walk to from {@code from}, 4-way, over
     * walkable tiles. Iterative (an explicit stack), so a 256x256 map cannot blow
     * the Java stack - the neighbour count is small but the fill is per frame.
     */
    private static void floodFillFrom(TilePosition from) {
        reachable = new boolean[GRID * GRID];

        if (!JBWEB.isWalkable(from)) return;

        int[] stack = new int[GRID * GRID];
        int top = 0;
        stack[top++] = from.y * GRID + from.x;
        reachable[from.y * GRID + from.x] = true;

        while (top > 0) {
            int index = stack[--top];
            int x = index % GRID;
            int y = index / GRID;

            top = tryPush(x + 1, y, top, stack);
            top = tryPush(x - 1, y, top, stack);
            top = tryPush(x, y + 1, top, stack);
            top = tryPush(x, y - 1, top, stack);
        }
    }

    private static int tryPush(int x, int y, int top, int[] stack) {
        if (x < 0 || y < 0 || x >= GRID || y >= GRID) return top;

        int index = y * GRID + x;
        if (reachable[index]) return top;

        if (!JBWEB.isWalkable(new TilePosition(x, y))) return top;

        reachable[index] = true;
        stack[top++] = index;
        return top;
    }

    private static Game game() {
        return JBWEB.game;
    }

    // ===========================================================================
    // Test seam
    // ===========================================================================

    /**
     * The walkable grid the flood fill reads, so a unit test can build a map
     * without a game. Deliberately a method and not a public field: callers that
     * only want to <i>ask</i> a question use {@link #reachable}, and the one
     * caller that supplies a grid is a test.
     */
    public static boolean[][] walkGridForTest() {
        return JBWEB.walkGrid;
    }
}
