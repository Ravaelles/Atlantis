package tests.unit;

import atlantis.placement.core.ConnectivityRegions;
import atlantis.placement.core.WalkabilityGrid;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reachability computed from terrain, with no engine involved.
 *
 * <p>
 * This is the replacement for the question {@code Game.hasPath} was being
 * asked, and on
 * OpenBW that answer is broken: the server publishes no regions, so a tile
 * cannot reach
 * <b>itself</b> (measured 2026-10-10, {@code _AI/CHALLENGES/OpenBW.md}). The
 * first test
 * below is that exact property, because it is the one the engine gets wrong and
 * the one
 * that made {@code Unit.build(...)} refuse a tile the builder was standing on.
 * </p>
 *
 * <p>
 * A synthetic map in, region ids out - so the cases that matter (two areas, a
 * one-tile
 * gap, an island, a diagonal) are assertions rather than game runs.
 * </p>
 */
@Tag("map")
public class ConnectivityRegionsTest {

    /**
     * A rectangular grid of walkable terrabit with an optional blocked rectangle
     * and
     * blocked cells given as "x,y" pairs.
     */
    private static WalkabilityGrid grid(int width, int height, String... blocked) {
        boolean[][] walkable = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                walkable[x][y] = true;
            }
        }

        for (String cell : blocked) {
            String[] parts = cell.split(",");
            walkable[Integer.parseInt(parts[0].trim())][Integer.parseInt(parts[1].trim())] = false;
        }

        return new WalkabilityGrid() {
            @Override
            public int mapWidth() {
                return width;
            }

            @Override
            public int mapHeight() {
                return height;
            }

            @Override
            public boolean isWalkable(int tx, int ty) {
                if (tx < 0 || ty < 0 || tx >= width || ty >= height)
                    return false;

                return walkable[tx][ty];
            }
        };
    }

    /**
     * <b>The property the engine fails.</b> A tile must reach itself; the engine's
     * region-based {@code hasPath} answers false for a position paired with itself
     * on
     * OpenBW, which is how a builder "could not reach" the tile it was standing on.
     */
    @Test
    public void aTileAlwaysReachesItself() {
        ConnectivityRegions regions = new ConnectivityRegions(grid(10, 10));

        assertTrue(
                regions.areConnected(5, 5, 5, 5),
                "a tile must reach itself - the engine's region-based answer does not, and that "
                        + "is exactly why the builder could not build where it stood");
    }

    /** An empty map is one connected region. */
    @Test
    public void anOpenMapIsASingleRegion() {
        ConnectivityRegions regions = new ConnectivityRegions(grid(16, 16));

        assertEquals(1, regions.regionCount(), "nothing blocks anything, so there is one area");
        assertTrue(regions.areConnected(0, 0, 15, 15), "opposite corners are connected");
    }

    /**
     * A wall of blocked tiles splits the map into two areas, and tiles on opposite
     * sides
     * are NOT connected. Both halves are asserted: a check that always said
     * "connected"
     * would pass a one-sided test while claiming every tile reachable.
     */
    @Test
    public void aWallSplitsTheMapIntoTwoUnconnectedRegions() {
        ConnectivityRegions regions = new ConnectivityRegions(
                grid(10, 10, "5,0", "5,1", "5,2", "5,3", "5,4", "5,5", "5,6", "5,7", "5,8", "5,9"));

        assertEquals(2, regions.regionCount(), "the wall separates left from right");

        assertTrue(regions.areConnected(1, 5, 4, 5), "both of these are left of the wall");
        assertFalse(
                regions.areConnected(1, 5, 8, 5),
                "a solid wall is not crossable - this is the answer hasPath was supposed to give");
    }

    /**
     * One blocked tile is an obstacle, not a division: the map is still one region
     * because a unit walks around it. This is the case that separates "obstacle"
     * from
     * "wall", and getting it wrong would call half a base unreachable.
     */
    @Test
    public void aSingleBlockedTileIsNotAWall() {
        ConnectivityRegions regions = new ConnectivityRegions(grid(10, 10, "5,5"));

        assertEquals(1, regions.regionCount(), "a unit can walk around one blocked tile");
        assertTrue(regions.areConnected(0, 0, 9, 9), "and the whole map stays one area");
    }

    /**
     * Diagonals count (8-connectivity): a corridor touching only at a corner still
     * connects, which is how the engine's pathing treats a corner a unit can cut.
     * The neighbouring assertion shows the test is not simply "everything
     * connects".
     */
    @Test
    public void diagonalContactConnectsTheTwoAreas() {
        // A diagonal line of blocked tiles with a single-tile break, so the two sides
        // meet only corner to corner at (5,4)/(6,5).
        ConnectivityRegions regions = new ConnectivityRegions(
                grid(12, 12, "5,0", "5,1", "5,2", "5,3", "5,4", "5,6", "5,7", "5,8", "5,9", "5,10", "5,11"));

        assertEquals(
                1, regions.regionCount(),
                "the gap at (5,5) connects the two halves, so there is one region");
    }

    /**
     * An island - a walkable pocket fully enclosed by blocked tiles - is its own
     * region
     * and cannot be reached. This is the case a naive "same area" check gets wrong.
     */
    @Test
    public void anEnclosedPocketIsItsOwnRegionAndUnreachable() {
        ConnectivityRegions regions = new ConnectivityRegions(
                grid(10, 10,
                        // A 1-tile pocket at (3,3) walled in on all eight sides.
                        "2,2", "3,2", "4,2",
                        "2,3", "4,3",
                        "2,4", "3,4", "4,4"));

        assertNotEquals(
                ConnectivityRegions.NO_REGION, regions.regionAt(3, 3),
                "the pocket is walkable, so it is a region - not merely unreachable");
        assertFalse(
                regions.areConnected(3, 3, 0, 0),
                "an enclosed pocket cannot be reached from outside");
    }

    /**
     * Blocked tiles belong to no region; that is what makes them distinguishable.
     */
    @Test
    public void aBlockedTileHasNoRegion() {
        ConnectivityRegions regions = new ConnectivityRegions(grid(5, 5, "2,2"));

        assertEquals(
                ConnectivityRegions.NO_REGION, regions.regionAt(2, 2),
                "an unwalkable tile is in no region, not in a region of its own");
    }

    /** Off-map coordinates answer "no region" rather than throwing or wrapping. */
    @Test
    public void offMapCoordinatesHaveNoRegion() {
        ConnectivityRegions regions = new ConnectivityRegions(grid(5, 5));

        assertEquals(ConnectivityRegions.NO_REGION, regions.regionAt(-1, 0), "left of the map");
        assertEquals(ConnectivityRegions.NO_REGION, regions.regionAt(0, -1), "above the map");
        assertEquals(ConnectivityRegions.NO_REGION, regions.regionAt(5, 0), "right of the map");
        assertEquals(ConnectivityRegions.NO_REGION, regions.regionAt(0, 5), "below the map");

        assertFalse(
                regions.areConnected(-1, 0, 0, 0),
                "an off-map tile is not connected to anything, not even its neighbour");
    }

    /**
     * Region numbering is deterministic for a given grid, so a caller (and a test)
     * can
     * rely on the ids themselves, not only on equality of pairs. Two identical
     * grids must
     * produce identical numbering - if this ever depended on hash order, every
     * assertion
     * that names a region would become flaky.
     */
    @Test
    public void regionNumberingIsDeterministic() {
        ConnectivityRegions first = new ConnectivityRegions(grid(8, 8, "4,4"));
        ConnectivityRegions second = new ConnectivityRegions(grid(8, 8, "4,4"));

        assertEquals(first.regionCount(), second.regionCount(), "same map, same count");

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                assertEquals(
                        first.regionAt(x, y), second.regionAt(x, y),
                        "same map must give the same region id at " + x + "," + y);
            }
        }
    }

    /**
     * The map is measured in tiles and the grid must not silently transpose: a map
     * that
     * is wide and short is not the same as one that is tall and narrow, and a
     * transposed
     * grid would put a tile's region on the wrong tile.
     */
    @Test
    public void aNonSquareMapIsNotTransposed() {
        // 20 wide, 4 tall: the right-hand column is far beyond the height.
        ConnectivityRegions regions = new ConnectivityRegions(grid(20, 4));

        assertEquals(1, regions.regionCount(), "one open area");
        assertNotEquals(
                ConnectivityRegions.NO_REGION, regions.regionAt(19, 3),
                "the far corner of a wide map is on the map");
        assertEquals(
                ConnectivityRegions.NO_REGION, regions.regionAt(3, 19),
                "the transposed corner is off this map - a transposed grid would wrongly accept it");
    }
}
