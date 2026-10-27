package atlantis.production.constructions.position;

/**
 * Final guard on legacy siting (owner 2026-10-23, NEXT #145): a standard
 * site beyond the search contract is rejected instead of built.
 * Siteless storms mint absurd tiles through the legacy chain (463
 * same-tile CANCELs in one game) - the catalogue will refuse them by
 * construction after cutover, but until then no builder walks across
 * the map to a tile the search never meant.
 *
 * <p>2026-10-24 correction (live Gateway storm, 4 "Null position"
 * cancels in 1 min): a literal 30-tile cap fights the spiral's own
 * contract (dynamic orders search radius 36) - in a crowded base the
 * only free tiles sit at ring edge 30-36 and a hard 30 converts them
 * into the exact "Can't find place (reason: OK)" + cancel storm it
 * was meant to prevent. The cap is therefore the wider of the user
 * cap and the order's radius: absurd-only, never ring-edge.</p>
 */
public final class BuildSiteDistance {

    /**
     * User cap on final build distance (tiles, ground).
     */
    public static final double MAX_SITE_GROUND_TILES = 30;

    private BuildSiteDistance() {
    }

    /**
     * Cap for this search: the wider of the user cap and the order's
     * own radius, so the guard never eats tiles the search was
     * explicitly allowed to return.
     */
    public static double capFor(double maxDistance) {
        if (maxDistance < 0) return MAX_SITE_GROUND_TILES;

        return Math.max(MAX_SITE_GROUND_TILES, maxDistance);
    }

    /**
     * True when the site is too far from the search base to build.
     * Null-safe: a missing site or base never blocks (the caller owns
     * those exits) - only a measured over-distance rejects.
     */
    public static boolean isTooFar(
        atlantis.map.position.HasPosition site, atlantis.map.position.HasPosition nearTo, double capTiles
    ) {
        if (site == null || site.position() == null) return false;
        if (nearTo == null || nearTo.position() == null) return false;
        if (capTiles < 0) return false;

        try {
            return site.groundDist(nearTo) > capTiles;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
