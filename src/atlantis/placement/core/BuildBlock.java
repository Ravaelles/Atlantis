package atlantis.placement.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A prefab layout: a rectangle that knows where its Pylon goes and which tiles
 * are
 * suitable for small (2x2), medium (3x2) and large (4x3) buildings
 * (`_AI/redesign/03_PLACEMENT.md` §1.1).
 *
 * <p>
 * This is the piece that encodes base-design knowledge for free: instead of
 * evaluating 1x1 tiles, a Block already knows the gateway spacing and which
 * spots
 * suit a tech building. Stardust ships 24 hand-designed templates; S2 ports the
 * shape and the few that pay for themselves, and more are added by writing
 * another
 * subclass - no caller changes.
 * </p>
 *
 * <p>
 * The Block itself is pure geometry: it is handed a
 * {@link TileAvailabilityGrid}
 * and either stamps itself or reports that it does not fit. It never reads the
 * game.
 * </p>
 */
public abstract class BuildBlock {

    private final int left;
    private final int top;

    protected BuildBlock(int left, int top) {
        this.left = left;
        this.top = top;
    }

    public abstract int width();

    public abstract int height();

    /** The offsets of the Pylon that powers this block, from its top-left. */
    protected abstract int powerPylonDx();

    protected abstract int powerPylonDy();

    protected abstract List<Slot> smallSlots();

    protected abstract List<Slot> mediumSlots();

    protected abstract List<Slot> largeSlots();

    public int left() {
        return left;
    }

    public int top() {
        return top;
    }

    public int powerPylonX() {
        return left + powerPylonDx();
    }

    public int powerPylonY() {
        return top + powerPylonDy();
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
     * Claims the block's tiles, so the next block cannot butt against it. The
     * grid marks the rectangle used and its ring as border.
     */
    public void stamp(TileAvailabilityGrid grid) {
        grid.markUsed(left, top, width(), height());
    }

    /** Every candidate slot the block offers, as absolute tiles. */
    public List<BuildLocation> locations() {
        List<BuildLocation> all = new ArrayList<>();
        addSlots(all, smallSlots(), 2, 2);
        addSlots(all, mediumSlots(), 3, 2);
        addSlots(all, largeSlots(), 4, 3);
        return Collections.unmodifiableList(all);
    }

    private void addSlots(List<BuildLocation> into, List<Slot> slots, int w, int h) {
        for (Slot slot : slots) {
            into.add(new BuildLocation(
                    left + slot.dx, top + slot.dy, w, h,
                    0, 0, 0, w >= 3));
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + left + "," + top + " "
                + width() + "x" + height() + ")";
    }
}
