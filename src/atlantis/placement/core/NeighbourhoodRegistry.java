package atlantis.placement.core;

/**
 * Where a neighbourhood is and which way its exit lies
 * (`_AI/redesign/03_PLACEMENT.md` §2 Step 5.2, §4.2).
 *
 * <p>
 * A neighbourhood is a base area: an origin (the mineral line) and an exit (the
 * main choke). Both are facts about the map, so they come through a port and
 * the
 * ranking that uses them stays pure.
 * </p>
 */
public final class NeighbourhoodRegistry {

    /** One base area, as the ranker needs it. */
    public static final class Neighbourhood {
        private final int originX;
        private final int originY;
        private final int exitX;
        private final int exitY;
        private final boolean hasExit;

        public Neighbourhood(int originX, int originY, int exitX, int exitY, boolean hasExit) {
            this.originX = originX;
            this.originY = originY;
            this.exitX = exitX;
            this.exitY = exitY;
            this.hasExit = hasExit;
        }

        public int originX() {
            return originX;
        }

        public int originY() {
            return originY;
        }

        public int exitX() {
            return exitX;
        }

        public int exitY() {
            return exitY;
        }

        /** False for a base whose choke is unknown (a hidden base, an island). */
        public boolean hasExit() {
            return hasExit;
        }
    }

    /** The map's base areas, as the ranking needs them. */
    public interface Source {
        /** The base nearest this tile - the neighbourhood a candidate belongs to. */
        Neighbourhood nearestTo(int tx, int ty);
    }

    private final Source source;

    public NeighbourhoodRegistry(Source source) {
        this.source = source;
    }

    /** Null when there is no base at all (before the Nexus exists). */
    public Neighbourhood forTile(int tx, int ty) {
        return source == null ? null : source.nearestTo(tx, ty);
    }

    /**
     * Ground distance in tiles from a spot to the neighbourhood's exit. Distance
     * is Chebyshev (tile steps), not a real path - a path query per candidate is
     * the expensive thing the catalogue exists to avoid, and Stardust makes the
     * same approximation before its own pathing pass.
     */
    public static int distanceToExit(Neighbourhood neighbourhood, int tx, int ty) {
        if (neighbourhood == null || !neighbourhood.hasExit())
            return Integer.MAX_VALUE;

        int dx = Math.abs(tx - neighbourhood.exitX());
        int dy = Math.abs(ty - neighbourhood.exitY());
        return Math.max(dx, dy);
    }
}
