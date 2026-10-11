package atlantis.placement.engine;

import atlantis.Atlantis;
import atlantis.map.AMap;
import atlantis.placement.core.WalkabilityGrid;

/**
 * {@link WalkabilityGrid} backed by the engine's terrain answers.
 *
 * <p>
 * The engine <b>does</b> publish walkability - it is the region graph that is
 * missing on
 * OpenBW, not the terrain - so this is a thin adapter and the one piece of the
 * reachability work that needs the game. Everything that consumes it
 * ({@code ConnectivityRegions}, {@code RegionReachability}) is pure and
 * testable with a
 * synthetic grid.
 * </p>
 *
 * <p>
 * Walkability, not buildability: reachability asks where a unit can
 * <em>walk</em>. A
 * buildable-but-unwalkable tile (a cliff face) is passable to nobody, and a
 * walkable-but-unbuildable one (a ramp) must not be treated as a wall.
 * </p>
 */
public final class EngineWalkabilityGrid implements WalkabilityGrid {

    @Override
    public int mapWidth() {
        return AMap.getMapWidthInTiles();
    }

    @Override
    public int mapHeight() {
        return AMap.getMapHeightInTiles();
    }

    @Override
    public boolean isWalkable(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= mapWidth() || ty >= mapHeight())
            return false;
        if (Atlantis.game() == null)
            return false;

        // Tile -> walk position -> the engine's answer. Asking per tile rather than per
        // walk position keeps this comparable with every other tile-based query in the
        // placement path, which is the whole point of routing reachability through a
        // grid.
        return Atlantis.game().isWalkable(new bwapi.WalkPosition(tx * 4, ty * 4));
    }
}
