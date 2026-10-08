package atlantis.placement.core;

import atlantis.placement.blocks.BlockTemplates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The catalogue of candidate build locations
 * ({@code _AI/redesign/03_PLACEMENT.md} §2 Step 5.1, §5.4 S1-S2).
 *
 * <p>
 * Two sources feed it, in this order:
 * </p>
 * <ol>
 * <li><b>Blocks</b> (S2) - prefab layouts are tried over the map first, because
 * their slots carry base-design knowledge a per-tile scan cannot express
 * (gateway spacing, which tiles suit tech). Every tile a block occupies is
 * stamped as used, so the next block cannot butt against it.</li>
 * <li><b>A per-tile scan</b> (S1) - whatever the blocks did not cover, found by
 * checking every free origin for each footprint. This is what makes the
 * catalogue useful before the full 24-template set exists, and it is also
 * the fallback for maps whose shape no template fits.</li>
 * </ol>
 *
 * <p>
 * Rebuilt only when the grid changed (a building placed or removed) - the
 * "lazy,
 * event-driven" rule Stardust uses, which is what lets a whole-map scan be
 * affordable.
 * </p>
 */
public final class BuildLocationCatalogue {

    /** Footprints the fallback scan answers for: the sizes a building uses here. */
    private static final int[][] SIZES = {
            { 2, 2 }, // Pylon, Cannon, Supply Depot
            { 3, 2 }, // Gateway, Cybernetics Core, Forge, Barracks
            { 4, 3 }, // Nexus, Robotics Facility, Stargate
    };

    private final TileAvailabilityGrid grid;
    private final List<BlockFactory> blockFactories;
    private final List<BuildLocation> all = new ArrayList<>();

    /** How a block is instantiated at a map position. */
    public interface BlockFactory {
        BuildBlock at(int left, int top);
    }

    public BuildLocationCatalogue(TileAvailabilityGrid grid) {
        this(grid, defaultBlockFactories());
    }

    public BuildLocationCatalogue(TileAvailabilityGrid grid, List<BlockFactory> blockFactories) {
        this.grid = grid;
        this.blockFactories = blockFactories;
        rebuild();
    }

    /** The templates this build ships (S2: all 24 normal + 4 start variants). */
    public static List<BlockFactory> defaultBlockFactories() {
        List<BlockFactory> factories = new ArrayList<>();

        for (final BuildBlock.Spec spec : BlockTemplates.normal()) {
            factories.add(new BlockFactory() {
                @Override
                public BuildBlock at(int left, int top) {
                    return new BuildBlock(spec, left, top);
                }
            });
        }

        for (final BuildBlock.Spec spec : BlockTemplates.startBlocks()) {
            factories.add(new BlockFactory() {
                @Override
                public BuildBlock at(int left, int top) {
                    return new BuildBlock(spec, left, top);
                }
            });
        }

        return factories;
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
        if (scorer != null)
            Collections.sort(matching, scorer);
        return matching;
    }

    /**
     * Rebuilds from scratch: stamp every block that fits, then fill the gaps with
     * the per-tile scan.
     */
    public void rebuild() {
        all.clear();
        stampBlocks();
        scanFreeTilesNotCoveredByBlocks();
    }

    /**
     * One pass, first fit wins per origin - deterministic, which is what the plan
     * requires. Stardust scans largest-template-first from the map centre so big
     * blocks get the interior; here the caller orders the factories and the origin
     * loop runs top-left to bottom-right. The ordering refinement is polish, not a
     * correctness question.
     */
    private void stampBlocks() {
        for (BlockFactory factory : blockFactories) {
            for (int x = 0; x <= grid.width(); x++) {
                for (int y = 0; y <= grid.height(); y++) {
                    BuildBlock block = factory.at(x, y);
                    if (x + block.width() > grid.width() || y + block.height() > grid.height())
                        continue;
                    if (!block.fits(grid))
                        continue;

                    all.addAll(block.locations());
                    block.stamp(grid);
                }
            }
        }
    }

    /**
     * Every free origin of each footprint the blocks did not already cover. The
     * scan skips tiles the grid marks used, so a block slot and a scanned tile can
     * never be the same candidate.
     */
    private void scanFreeTilesNotCoveredByBlocks() {
        for (int[] size : SIZES) {
            int w = size[0];
            int h = size[1];

            for (int x = 0; x <= grid.width() - w; x++) {
                for (int y = 0; y <= grid.height() - h; y++) {
                    if (!grid.isFreeFor(x, y, w, h))
                        continue;

                    all.add(new BuildLocation(
                            x, y, w, h,
                            0, // builderFrames: the planner computes it (needs workers)
                            0, // available now: the grid already excluded used tiles
                            0, // distanceToExit: the planner fills it from the neighbourhood
                            false // tech-location: a block concern
                    ));
                }
            }
        }
    }

    /**
     * Marks a tile reserved for the rest of the pass, so a second building in the
     * same pass cannot take it (Stardust erases the location from its list; the
     * grid flag is the same idea with one place to reset).
     */
    public void reserve(BuildLocation location) {
        grid.markUsed(location.tileX(), location.tileY(), location.tileWidth(), location.tileHeight());
    }

    /**
     * Ordering hook; the planner installs the real one, tests a deterministic fake.
     */
    public interface LocationScorer extends Comparator<BuildLocation> {
    }
}
