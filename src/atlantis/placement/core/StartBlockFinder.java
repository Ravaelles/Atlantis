package atlantis.placement.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Picks the start-block anchor for a base (`_AI/redesign/03_PLACEMENT.md` §2
 * Step 1.1, §5.4 S2/C4).
 *
 * <p>
 * The anchor is the layout a base is built around: a start block gives
 * approximately two Gateways, two to four tech buildings and three to four
 * cannon
 * spots, and Stardust tries a prioritized list of variants until one fits, then
 * keeps it as that base's block for the rest of the game.
 * </p>
 *
 * <p>
 * Pure: it is handed the grid and the templates, and answers the first variant
 * that fits near the base plus the tile it was stamped at. Which variant is
 * "first" is the template list's order, so this is deterministic.
 * </p>
 */
public final class StartBlockFinder {

    /** A chosen anchor: the template and where it was placed. */
    public static final class Anchor {
        private final BuildBlock.Spec spec;
        private final int left;
        private final int top;

        Anchor(BuildBlock.Spec spec, int left, int top) {
            this.spec = spec;
            this.left = left;
            this.top = top;
        }

        public BuildBlock.Spec spec() {
            return spec;
        }

        public int left() {
            return left;
        }

        public int top() {
            return top;
        }

        /** The block at its chosen position, for reading its slots. */
        public BuildBlock block() {
            return new BuildBlock(spec, left, top);
        }

        @Override
        public String toString() {
            return spec.name + "@(" + left + "," + top + ")";
        }
    }

    private final TileAvailabilityGrid grid;
    private final List<BuildBlock.Spec> startBlocks;

    public StartBlockFinder(TileAvailabilityGrid grid, List<BuildBlock.Spec> startBlocks) {
        this.grid = grid;
        this.startBlocks = startBlocks;
    }

    /**
     * The first start-block variant that fits with its whole footprint inside the
     * search window around the base, or null when none does.
     *
     * <p>
     * The base's own tile is the search centre: a start block is an anchor for
     * <i>this</i> base, so a variant that only fits on the far side of the map is
     * no answer. The window is generous (the block may sit beside the Nexus, not on
     * it), but bounded so the search is cheap and the result sensible.
     * </p>
     */
    public Anchor findFor(int baseX, int baseY, int searchRadius) {
        for (BuildBlock.Spec spec : startBlocks) {
            Anchor anchor = tryVariant(spec, baseX, baseY, searchRadius);
            if (anchor != null)
                return anchor;
        }
        return null;
    }

    private Anchor tryVariant(BuildBlock.Spec spec, int baseX, int baseY, int searchRadius) {
        // Prefer the placement closest to the base: a deterministic scan of the
        // window, ordered by distance, so the same map always yields the same anchor.
        List<int[]> origins = originsByDistance(spec, baseX, baseY, searchRadius);

        for (int[] origin : origins) {
            BuildBlock candidate = new BuildBlock(spec, origin[0], origin[1]);
            if (candidate.fits(grid))
                return new Anchor(spec, origin[0], origin[1]);
        }

        return null;
    }

    private List<int[]> originsByDistance(
            BuildBlock.Spec spec, final int baseX, final int baseY, int searchRadius) {
        List<int[]> origins = new ArrayList<>();

        int minX = Math.max(0, baseX - searchRadius);
        int maxX = Math.min(grid.width() - spec.width, baseX + searchRadius);
        int minY = Math.max(0, baseY - searchRadius);
        int maxY = Math.min(grid.height() - spec.height, baseY + searchRadius);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                origins.add(new int[] { x, y });
            }
        }

        java.util.Collections.sort(origins, new java.util.Comparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                return Integer.compare(dist(a, baseX, baseY), dist(b, baseX, baseY));
            }
        });

        return origins;
    }

    private static int dist(int[] origin, int baseX, int baseY) {
        return Math.max(Math.abs(origin[0] - baseX), Math.abs(origin[1] - baseY));
    }
}
