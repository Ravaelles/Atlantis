package tests.unit;

import atlantis.placement.core.WalkabilityGrid;
import atlantis.placement.engine.RegionReachability;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@code Reachability} implementation that does not ask the engine - and
 * therefore
 * cannot inherit the engine's failure on OpenBW.
 *
 * <p>
 * The contract under test is the one the port declares: <b>a tile always
 * reaches
 * itself</b>. That sounds trivial and is exactly what the engine gets wrong on
 * OpenBW
 * ({@code hasPath} is region-based and the server publishes no regions, so it
 * answers
 * false for a position against itself) - which is how a builder came to be
 * unable to
 * build on the tile it was standing on.
 * </p>
 */
@Tag("placement")
public class RegionReachabilityTest {

    private static WalkabilityGrid openMap(int size) {
        return new WalkabilityGrid() {
            @Override
            public int mapWidth() {
                return size;
            }

            @Override
            public int mapHeight() {
                return size;
            }

            @Override
            public boolean isWalkable(int tx, int ty) {
                return tx >= 0 && ty >= 0 && tx < size && ty < size;
            }
        };
    }

    private static WalkabilityGrid mapWithWalls(int size, String... blocked) {
        boolean[][] walkable = new boolean[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
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
                return size;
            }

            @Override
            public int mapHeight() {
                return size;
            }

            @Override
            public boolean isWalkable(int tx, int ty) {
                if (tx < 0 || ty < 0 || tx >= size || ty >= size)
                    return false;
                return walkable[tx][ty];
            }
        };
    }

    /**
     * <b>The property that unblocks the builder.</b> Standing on a tile must count
     * as
     * reaching it - the engine's region-based answer says no, which is the refusal
     * this
     * whole exercise is about.
     */
    @Test
    public void aTileReachesItselfEvenOnAnEmptyMap() {
        RegionReachability reachability = new RegionReachability(openMap(32));

        assertTrue(
                reachability.canReach(20, 20, 20, 20),
                "the builder must be able to reach the tile it is standing on");
    }

    /** Two tiles in the same open area are connected; that is the normal case. */
    @Test
    public void tilesInOneOpenAreaAreConnected() {
        RegionReachability reachability = new RegionReachability(openMap(32));

        assertTrue(reachability.canReach(0, 0, 31, 31), "an open map is one area");
    }

    /**
     * A wall of blocked tiles divides the map, and the two sides are NOT connected.
     * This
     * is the half that stops the implementation from being "always true", which
     * would
     * pass the reflexivity test while claiming a walled-off area is reachable.
     */
    @Test
    public void tilesSeparatedByAWallAreNotConnected() {
        RegionReachability reachability = new RegionReachability(
                mapWithWalls(12, "6,0", "6,1", "6,2", "6,3", "6,4", "6,5", "6,6", "6,7", "6,8", "6,9", "6,10", "6,11"));

        assertTrue(reachability.canReach(1, 6, 5, 6), "both are left of the wall");
        assertFalse(reachability.canReach(1, 6, 10, 6), "the wall is not crossable");

        assertEquals(2, reachability.regionCount(), "the wall makes exactly two areas");
    }

    /**
     * An unwalkable tile is reached by nothing, including itself - and may not be.
     */
    @Test
    public void anUnwalkableTileIsNotReachable() {
        RegionReachability reachability = new RegionReachability(mapWithWalls(10, "5,5"));

        assertFalse(
                reachability.canReach(5, 5, 5, 5),
                "the reflexivity rule is about WALKABLE tiles; an obstacle is not reachable");
    }

    /**
     * An empty map is one area - the count is exposed for diagnostics and pinning.
     */
    @Test
    public void anEmptyMapHasOneRegion() {
        assertEquals(1, new RegionReachability(openMap(16)).regionCount(), "nothing blocks anything");
    }
}
