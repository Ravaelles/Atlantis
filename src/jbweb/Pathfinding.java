package jbweb;

import bwapi.*;

public class Pathfinding {
    static PathCache unitPathCache = new PathCache();

    static int maxCacheSize = 10000;

    /// Clears the entire Pathfinding cache. All Paths will be generated as a new Path.
    public static void clearCache() {
        unitPathCache.indexList.clear();
        unitPathCache.pathCacheIndex = 0;
        unitPathCache.pathCache.clear();
    }

    /// Returns true if the TilePosition is walkable (does not include any buildings).
    public static boolean terrainWalkable(TilePosition tile) {
        return JBWEB.isWalkable(tile);
    }

    /// Returns true if the TilePosition is walkable (includes buildings).
    public static boolean unitWalkable(TilePosition tile) {
        return JBWEB.isWalkable(tile) && JBWEB.isUsed(tile, 1, 1) == UnitType.None;
    }
}
