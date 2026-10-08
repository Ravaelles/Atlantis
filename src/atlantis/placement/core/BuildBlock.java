package atlantis.placement.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A prefab layout: a rectangle that knows where its Pylon goes and which of its
 * tiles suit small (2x2), medium (3x2) and large (4x3) buildings
 * (`_AI/redesign/03_PLACEMENT.md` §1.1).
 *
 * <p>
 * This is the piece that encodes base-design knowledge for free: instead of
 * evaluating 1x1 tiles, a template already knows the gateway spacing and which
 * spots suit a tech building. Stardust ships 24 hand-written C++ classes; here one
 * template is a <b>data row</b> ({@link Spec}), so the whole set is a table rather
 * than 24 classes - less code, and adding a template is adding a row.
 * </p>
 *
 * <p>
 * Pure geometry: a template is handed a {@link TileAvailabilityGrid} and either
 * stamps itself or reports that it does not fit. It never reads the game.
 * </p>
 */
public final class BuildBlock {

    /** One template's geometry. Offsets are from the block's top-left. */
    public static final class Spec {
        public final String name;
        public final int width;
        public final int height;
        public final int pylonDx;
        public final int pylonDy;
        final int[][] small;
        final int[][] medium;
        final int[][] mediumNoExit;
        final int[][] large;

        public Spec(
            String name, int width, int height, int pylonDx, int pylonDy,
            int[][] small, int[][] medium, int[][] mediumNoExit, int[][] large
        ) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.pylonDx = pylonDx;
            this.pylonDy = pylonDy;
            this.small = small;
            this.medium = medium;
            this.mediumNoExit = mediumNoExit;
            this.large = large;
        }
    }

    private final Spec spec;
    private final int left;
    private final int top;

    public BuildBlock(Spec spec, int left, int top) {
        this.spec = spec;
        this.left = left;
        this.top = top;
    }

    public String name() {
        return spec.name;
    }

    public int left() {
        return left;
    }

    public int top() {
        return top;
    }

    public int width() {
        return spec.width;
    }

    public int height() {
        return spec.height;
    }

    public int powerPylonX() {
        return left + spec.pylonDx;
    }

    public int powerPylonY() {
        return top + spec.pylonDy;
    }

    /**
     * Does the whole block fit on free terrain? Every tile of the rectangle must
     * be free - the same "the entire footprint" rule as
     * {@link TileAvailabilityGrid#isFreeFor}.
     */
    public boolean fits(TileAvailabilityGrid grid) {
        return grid.isFreeFor(left, top, width(), height());
    }

    /**
     * Claims the block's tiles so the next block cannot butt against it. The grid
     * marks the rectangle used and its ring as border.
     */
    public void stamp(TileAvailabilityGrid grid) {
        grid.markUsed(left, top, width(), height());
    }

    /** Every candidate slot this template offers, as absolute tiles. */
    public List<BuildLocation> locations() {
        List<BuildLocation> all = new ArrayList<>();
        add(all, spec.small, 2, 2, false, true);
        add(all, spec.medium, 3, 2, true, true);
        add(all, spec.mediumNoExit, 3, 2, true, false);
        add(all, spec.large, 4, 3, true, true);
        return Collections.unmodifiableList(all);
    }

    private void add(
        List<BuildLocation> into, int[][] offsets, int w, int h, boolean tech, boolean hasExit
    ) {
        if (offsets == null) return;

        for (int[] offset : offsets) {
            into.add(new BuildLocation(
                left + offset[0], top + offset[1], w, h,
                0,      // builderFrames: the planner refines it
                0,      // available now: the grid already excluded used tiles
                0,      // distanceToExit: the planner fills it from the neighbourhood
                tech,   // medium/large suit tech buildings
                hasExit
            ));
        }
    }

    @Override
    public String toString() {
        return spec.name + "(" + left + "," + top + ")";
    }
}
