package atlantis.production.v2;

import java.util.List;

/**
 * Scores a candidate Pylon tile by how many unpowered buildable neighbours it
 * would power (01_PRODUCTION.md, "BuildPositionResolver": a Pylon is placed
 * where it unlocks the most building room, not where the finder happens to land
 * first).
 *
 * <p>
 * Pure and geometry-only: it takes rectangles in build tiles and answers a
 * number. The map is read once, by whoever builds the candidates, so the rule
 * itself is testable without a game and cannot drift with map state - which is
 * the same split the rest of v2 uses ({@link ResourceTimeline} knows nothing
 * about BWAPI either).
 * </p>
 *
 * <p>
 * A tile's score is the count of free, unpowered build tiles within the Pylon's
 * power radius. Tiles that already have power are worth nothing (a second Pylon
 * covering the same ground is wasted minerals), and neither are blocked ones
 * (powering a tile nobody can build on buys nothing).
 * </p>
 */
public final class PylonPlacementScore {

    /**
     * A Pylon powers a radius of 7 build tiles around itself in StarCraft; this
     * is the square bound used for the count (the engine uses a circle, but for
     * ranking candidate tiles the difference only shifts near-ties, which the
     * caller resolves by distance anyway).
     */
    public static final int POWER_RADIUS_TILES = 7;

    private PylonPlacementScore() {
    }

    /**
     * Scores one candidate tile.
     *
     * @param tileX          the candidate Pylon's tile
     * @param tileY          the candidate Pylon's tile
     * @param buildableTiles tiles a building could occupy (caller-read, so this
     *                       stays pure)
     * @param alreadyPowered tiles already inside another Pylon's radius
     * @return how many buildable, unpowered tiles this Pylon would unlock
     */
    public static int score(int tileX, int tileY, List<Tile> buildableTiles, List<Tile> alreadyPowered) {
        int unlocked = 0;

        for (Tile tile : buildableTiles) {
            if (!withinPowerRadius(tileX, tileY, tile))
                continue;
            if (contains(alreadyPowered, tile))
                continue;

            unlocked++;
        }

        return unlocked;
    }

    /**
     * The best candidate among several, or {@code null} when there are none.
     * Ties keep the FIRST candidate, so the caller's order is the tie-breaker -
     * which is how "the position finder's own preference" survives this rule
     * instead of being overwritten by it.
     */
    public static Tile best(List<Tile> candidates, List<Tile> buildableTiles, List<Tile> alreadyPowered) {
        Tile best = null;
        int bestScore = -1;

        for (Tile candidate : candidates) {
            int score = score(candidate.x, candidate.y, buildableTiles, alreadyPowered);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best;
    }

    private static boolean withinPowerRadius(int tileX, int tileY, Tile tile) {
        return Math.abs(tile.x - tileX) <= POWER_RADIUS_TILES
                && Math.abs(tile.y - tileY) <= POWER_RADIUS_TILES;
    }

    private static boolean contains(List<Tile> tiles, Tile tile) {
        for (Tile other : tiles) {
            if (other.x == tile.x && other.y == tile.y)
                return true;
        }
        return false;
    }

    /** A build tile, as a value - the only geometry type this class needs. */
    public static final class Tile {
        public final int x;
        public final int y;

        public Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }
}
