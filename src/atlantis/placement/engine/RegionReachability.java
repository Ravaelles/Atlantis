package atlantis.placement.engine;

import atlantis.placement.core.ConnectivityRegions;
import atlantis.placement.core.Reachability;
import atlantis.placement.core.WalkabilityGrid;

/**
 * Reachability answered from terrain, by {@link ConnectivityRegions} - no
 * engine
 * involvement at all.
 *
 * <p>
 * This is the implementation that works where the engine does not. On OpenBW
 * {@code Game.hasPath} is region-based and the server publishes no regions, so
 * it
 * answers {@code false} for every pair of tiles; a walkable-area labelling
 * cannot have
 * that failure, because it never asks the engine anything.
 * </p>
 *
 * <p>
 * The region labelling is computed once, in the constructor, and is <b>not</b>
 * rebuilt
 * when a building appears: a building is not an obstacle to the builder walking
 * onto its
 * own site, so treating it as one would refuse the very placements this exists
 * to allow.
 * A caller that needs the stricter question (is there a path a <i>different</i>
 * unit could
 * take, around a new building) needs a different grid, not a different answer
 * from this
 * one.
 * </p>
 *
 * <p>
 * <b>Reflexivity is structural here</b> rather than asserted:
 * {@code canReach(a, a)}
 * compares a region id with itself, so it is true whenever the tile is walkable
 * at all -
 * the property the engine's answer fails.
 * </p>
 */
public final class RegionReachability implements Reachability {

    private final ConnectivityRegions regions;

    public RegionReachability(WalkabilityGrid grid) {
        this.regions = new ConnectivityRegions(grid);
    }

    @Override
    public boolean canReach(int fromTx, int fromTy, int toTx, int toTy) {
        return regions.areConnected(fromTx, fromTy, toTx, toTy);
    }

    /**
     * The area count, exposed for diagnostics and for tests that pin a specific
     * map.
     */
    public int regionCount() {
        return regions.regionCount();
    }
}
