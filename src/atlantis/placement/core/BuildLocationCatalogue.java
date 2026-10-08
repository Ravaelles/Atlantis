package atlantis.placement.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The catalogue of candidate build locations, grouped by footprint width
 * (`_AI/redesign/03_PLACEMENT.md` §2 Step 5.1).
 *
 * <p>
 * S1 keeps this deliberately simple: instead of Stardust's 24 hand-designed
 * Block templates, it scans the free tiles of the map once and records every
 * origin where a {@code w x h} footprint fits. That already gives the scheduler
 * something the legacy finder never had - a <b>ranked list</b> of validated
 * tiles
 * to choose between - and it is the layer the Block templates plug into later
 * (S2) without changing any caller.
 * </p>
 *
 * <p>
 * A location is validated against {@link TileAvailabilityGrid}, so "free" means
 * the whole footprint is free of terrain, resources, depots and earlier
 * reservations in the same pass.
 * </p>
 */
public final class BuildLocationCatalogue {

    /** Footprints the catalogue answers for: the sizes a building uses here. */
    private static final int[][] SIZES = {
            { 2, 2 }, // Pylon, Cannon, Supply Depot
            { 3, 2 }, // Gateway, Cybernetics Core, Forge, Barracks
            { 4, 3 }, // Nexus, Robotics Facility, Stargate
    };

    private final TileAvailabilityGrid grid;
    private final List<BuildLocation> all = new ArrayList<>();

    public BuildLocationCatalogue(TileAvailabilityGrid grid) {
        this.grid = grid;
        rebuild(Collections.<LocationScorer>emptyList());
    }

    /** Everything found, unordered. */
    public List<BuildLocation> all() {
        return Collections.unmodifiableList(all);
    }

    /** Candidates of an exact footprint, best first. */
    public List<BuildLocation> candidates(int tileWidth, int tileHeight, LocationScorer scorer) {
        List<BuildLocation> matching = new ArrayList<>();
        for (BuildLocation location : all) {
            if (location.tileWidth() == tileWidth && location.tileHeight() == tileHeight) {
                matching.add(location);
            }
        }
        return ranked(matching, scorer);
    }

    /**
     * Rebuilds by scanning every free tile for each footprint. O(map area x
     * sizes), run only when the grid changed (a building placed or removed) -
     * the same "lazy, event-driven" rule Stardust uses.
     */
    public void rebuild(List<LocationScorer> scorers) {
        all.clear();

        for (int[] size : SIZES) {
            int w = size[0];
            int h = size[1];

            for (int x = 0; x <= grid.width() - w; x++) {
                for (int y = 0; y <= grid.height() - h; y++) {
                    if (!grid.isFreeFor(x, y, w, h))
                        continue;

                    all.add(new BuildLocation(
                            x, y, w, h,
                            0, // builderFrames: unknown here, the planner refines it
                            0, // free now: the grid already excluded used tiles
                            0, // distanceToExit: filled by the planner when it has a neighbourhood
                            false // tech-location: a Block-template concept (S2+)
                    ));
                }
            }
        }
    }

    private List<BuildLocation> ranked(List<BuildLocation> candidates, LocationScorer scorer) {
        if (scorer != null) {
            Collections.sort(candidates, scorer);
        }
        return candidates;
    }

    /**
     * Ordering hook; the planner installs the real one, tests a deterministic fake.
     */
    public interface LocationScorer extends Comparator<BuildLocation> {
    }
}
