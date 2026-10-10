package tests.unit;

import bwapi.TilePosition;
import jbweb.JBWEB;
import jbweb.Pathfinding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reachability over our own tile grid, the answer the engine cannot give on
 * OpenBW.
 *
 * <p>
 * {@code Game.hasPath} is a region-group comparison on OpenBW and the engine's
 * region table is empty there, so it returns false for every pair - including a
 * tile against itself ({@code _AI/CHALLENGES/OpenBW-API.md}). The bot's own
 * model
 * ({@code JBWEB.walkGrid}, built from {@code Game.isWalkable}) is healthy, so
 * the
 * answer comes from a flood fill over it, the shape PurpleWave uses.
 * </p>
 *
 * <p>
 * These tests drive the grid directly rather than a game world: the unit under
 * test is a pure function of {@code walkGrid}, which is exactly why it is cheap
 * and why it can be pinned here instead of in a game.
 * </p>
 */
public class PathfindingReachableTest {

    private static void clearGrid() {
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                Pathfinding.walkGridForTest()[x][y] = false;
            }
        }
        Pathfinding.clearCache();
    }

    private static void walkable(int x, int y) {
        Pathfinding.walkGridForTest()[x][y] = true;
    }

    /** An empty grid has no path anywhere - and must not crash or loop. */
    @Test
    public void nothingIsReachableOnAnEmptyGrid() {
        clearGrid();

        assertFalse(Pathfinding.reachable(new TilePosition(0, 0), new TilePosition(1, 0)),
                "an unwalkable source reaches nothing");
    }

    /** Adjacent walkable tiles are reachable, both ways. */
    @Test
    public void adjacentWalkableTilesAreReachableBothWays() {
        clearGrid();
        walkable(10, 10);
        walkable(11, 10);

        assertTrue(Pathfinding.reachable(new TilePosition(10, 10), new TilePosition(11, 10)),
                "the neighbour is reachable");
        assertTrue(Pathfinding.reachable(new TilePosition(11, 10), new TilePosition(10, 10)),
                "reachability is symmetric");
    }

    /**
     * The measured OpenBW failure, in miniature: two tiles on water with no way
     * around must NOT be called reachable just because both are walkable on their
     * own. This is the difference between a walkability test and a real path.
     */
    @Test
    public void disconnectedWalkableRegionsAreNotReachable() {
        clearGrid();
        walkable(5, 5);
        walkable(50, 50);

        assertFalse(Pathfinding.reachable(new TilePosition(5, 5), new TilePosition(50, 50)),
                "two isolated walkable tiles are not connected - a walkability check "
                        + "would wrongly say yes, which is the bug this replaces");
    }

    /** Around a wall of obstacles, a path that exists must be found. */
    @Test
    public void aPathAroundAnObstacleIsFound() {
        clearGrid();

        // A 3x3 room around (20,20), with a wall column at x=22 that has a gap at
        // y=24 only. Reaching (25,20) means going round through the gap.
        for (int x = 18; x <= 26; x++) {
            for (int y = 18; y <= 26; y++) {
                walkable(x, y);
            }
        }
        for (int y = 18; y <= 26; y++) {
            if (y == 24)
                continue; // the gap
            Pathfinding.walkGridForTest()[22][y] = false;
        }
        Pathfinding.clearCache();

        assertTrue(Pathfinding.reachable(new TilePosition(20, 20), new TilePosition(25, 20)),
                "the far side is reachable through the gap in the wall");
    }

    /** The same wall with no gap: the far side is genuinely unreachable. */
    @Test
    public void aSolidWallBlocksReachability() {
        clearGrid();

        for (int x = 18; x <= 26; x++) {
            for (int y = 18; y <= 26; y++) {
                walkable(x, y);
            }
        }
        for (int y = 18; y <= 26; y++) {
            Pathfinding.walkGridForTest()[22][y] = false;
        }
        Pathfinding.clearCache();

        assertFalse(Pathfinding.reachable(new TilePosition(20, 20), new TilePosition(25, 20)),
                "a solid wall blocks the far side");
    }

    /** A tile reaches itself - the engine answered false for this on OpenBW. */
    @Test
    public void aTileReachesItself() {
        clearGrid();
        walkable(7, 7);

        assertTrue(Pathfinding.reachable(new TilePosition(7, 7), new TilePosition(7, 7)),
                "a walkable tile reaches itself, which the engine's hasPath could not answer");
    }

    /** The flood fill is cached per source, so a second query does not refill. */
    @Test
    public void cacheIsReusedForTheSameSource() {
        clearGrid();
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                walkable(x, y);
            }
        }
        Pathfinding.clearCache();

        assertTrue(Pathfinding.reachable(new TilePosition(1, 1), new TilePosition(19, 19)));

        // Carve a hole after the fill; a cached fill must still answer from the
        // grid as it was, and clearing the cache must pick the change up. This is
        // the contract the unit lifecycle relies on (onUnitDiscover/onUnitDestroy
        // call clearCache).
        Pathfinding.walkGridForTest()[10][10] = false;
        assertTrue(Pathfinding.reachable(new TilePosition(1, 1), new TilePosition(19, 19)),
                "a cached fill is not invalidated until clearCache() - documented, not accidental");

        Pathfinding.clearCache();
        assertTrue(Pathfinding.reachable(new TilePosition(1, 1), new TilePosition(19, 19)),
                "a single blocked tile does not disconnect a 20x20 open room");
    }
}
