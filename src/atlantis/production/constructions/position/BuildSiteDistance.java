package atlantis.production.constructions.position;

/**
 * Final guard on legacy siting (owner 2026-10-23, NEXT #145): a standard
 * site further than this from the search base is rejected instead of
 * built. Siteless storms mint absurd tiles through the legacy chain
 * (463 same-tile CANCELs in one game) - the catalogue will refuse them
 * by construction after cutover, but until then no Pylon walks 40 tiles
 * to a tile the spiral never meant.
 */
public final class BuildSiteDistance {

    private BuildSiteDistance() {
    }

    /** No standard building breaks ground further than this from its search base. */
    public static final double MAX_SITE_GROUND_TILES = 30;

    /**
     * True when the site is too far from the search base to build.
     * Null-safe: a missing site or base never blocks (the caller owns
     * those exits) - only a measured over-distance rejects.
     */
    public static boolean isTooFar(
        atlantis.map.position.HasPosition site, atlantis.map.position.HasPosition nearTo
    ) {
        return isTooFar(site, nearTo, MAX_SITE_GROUND_TILES);
    }

    static boolean isTooFar(
        atlantis.map.position.HasPosition site, atlantis.map.position.HasPosition nearTo, double maxTiles
    ) {
        if (site == null || site.position() == null) return false;
        if (nearTo == null || nearTo.position() == null) return false;
        if (maxTiles < 0) return false;

        try {
            return site.groundDist(nearTo) > maxTiles;
        }
        catch (RuntimeException ignored) {
            return false;
        }
    }
}
