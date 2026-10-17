package atlantis.placement.engine;

import atlantis.placement.core.CoverageVerdict;
import atlantis.map.analysis.DefenseSpots;
import atlantis.map.position.APosition;
import atlantis.placement.core.BuildLocation;
import atlantis.placement.core.BuildLocationCatalogue;
import atlantis.placement.core.NeighbourhoodRegistry;

/**
 * Ordering for a defensive structure by weapon coverage, not by distance.
 *
 * <p>
 * A Photon Cannon exists to shoot what comes through the choke, so a tile
 * whose weapon range covers the choke line beats a nearer tile that cannot
 * shoot it - which the distance-only {@code ChokeAffinityRanker} gets
 * backwards on wide or angled chokes. Choke ends + anchor + range arrive
 * resolved (the catalogue resolves them once per call); this only compares.
 * Availability still orders first: a tile powered now beats one that waits.
 * </p>
 */
public final class CoverageCannonRanker implements BuildLocationCatalogue.LocationScorer {

    private final APosition endA;
    private final APosition endB;
    private final APosition anchor;
    private final double rangePx;

    public CoverageCannonRanker(APosition endA, APosition endB, APosition anchor, int rangeTiles) {
        this.endA = endA;
        this.endB = endB;
        this.anchor = anchor;
        this.rangePx = rangeTiles * 32.0;
    }

    @Override
    public int compare(BuildLocation a, BuildLocation b) {
        int availability = AvailabilityFirst.compare(a, b);
        if (availability != 0)
            return availability;

        boolean aChoke = coversChoke(a);
        boolean bChoke = coversChoke(b);
        if (aChoke != bChoke) return aChoke ? -1 : 1;

        boolean aAnchor = coversAnchor(a);
        boolean bAnchor = coversAnchor(b);
        if (aAnchor != bAnchor) return aAnchor ? -1 : 1;

        return Double.compare(distToChoke(a), distToChoke(b));
    }

    private boolean coversChoke(BuildLocation location) {
        return distToChoke(location) <= rangePx;
    }

    /**
     * The verdict for one tile, for the twin: same predicates as the
     * compare, exposed so a game log can attribute what the ranking
     * decided.
     */
    @Override
    public CoverageVerdict coverageOf(BuildLocation location) {
        return new CoverageVerdict(coversChoke(location), coversAnchor(location));
    }

    private double distToChoke(BuildLocation location) {
        return DefenseSpots.distPointToSegmentPx(centerOf(location), endA, endB);
    }

    private boolean coversAnchor(BuildLocation location) {
        if (anchor == null) return false;
        APosition middle = centerOf(location);
        return Math.hypot(middle.getX() - anchor.getX(), middle.getY() - anchor.getY()) <= rangePx;
    }

    private static APosition centerOf(BuildLocation location) {
        return DefenseSpots.tileCenter(
            location.tileX(), location.tileY(), location.tileWidth(), location.tileHeight());
    }
}
