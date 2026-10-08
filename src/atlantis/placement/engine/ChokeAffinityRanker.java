package atlantis.placement.engine;

import atlantis.placement.core.BuildLocation;
import atlantis.placement.core.BuildLocationCatalogue;
import atlantis.placement.core.NeighbourhoodRegistry;

/**
 * Ordering for a defensive structure: closest to the neighbourhood's choke
 * first
 * (`_AI/redesign/03_PLACEMENT.md` §2 Phase 4, §5.4 S4).
 *
 * <p>
 * A Photon Cannon exists to shoot what comes through the choke, so its
 * placement
 * is dominated by that distance - unlike a Gateway, which the generic ranker
 * pushes toward the map. The <b>decision</b> to build a cannon, how many and
 * when,
 * is policy and stays in S5; this only answers "given that one is wanted, which
 * tiles suit it".
 * </p>
 *
 * <p>
 * A cannon is also power-independent in the sense that it needs Psi but shoots
 * without it, so availability is still ordered first: a tile that is powered
 * now
 * beats one that will be.
 * </p>
 */
public final class ChokeAffinityRanker implements BuildLocationCatalogue.LocationScorer {

    private final NeighbourhoodRegistry neighbourhoods;

    public ChokeAffinityRanker(NeighbourhoodRegistry neighbourhoods) {
        this.neighbourhoods = neighbourhoods;
    }

    @Override
    public int compare(BuildLocation a, BuildLocation b) {
        int availability = Integer.compare(normalise(a.framesUntilAvailable()),
                normalise(b.framesUntilAvailable()));
        if (availability != 0)
            return availability;

        return Integer.compare(chokeDistance(a), chokeDistance(b));
    }

    private static int normalise(int framesUntilAvailable) {
        return framesUntilAvailable < 0 ? Integer.MAX_VALUE : framesUntilAvailable;
    }

    private int chokeDistance(BuildLocation location) {
        NeighbourhoodRegistry.Neighbourhood area = neighbourhoods.forTile(location.tileX(), location.tileY());

        int distance = NeighbourhoodRegistry.distanceToExit(area, location.tileX(), location.tileY());
        // No known choke: fall back to builder travel so a placement still happens.
        return distance == Integer.MAX_VALUE ? location.builderFrames() : distance;
    }
}
