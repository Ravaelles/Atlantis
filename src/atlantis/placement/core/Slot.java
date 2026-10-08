package atlantis.placement.core;

/**
 * A candidate spot inside a {@link BuildBlock}, in offsets from the block's
 * top-left. Pure geometry.
 */
public final class Slot {

    public final int dx;
    public final int dy;

    /**
     * Medium slots only: whether a building here can reach an exit (Stardust's
     * {@code hasExit}). Medium slots without one are preferred for buildings that
     * do not need to send units toward the map.
     */
    public final boolean hasExit;

    public Slot(int dx, int dy, boolean hasExit) {
        this.dx = dx;
        this.dy = dy;
        this.hasExit = hasExit;
    }

    public Slot(int dx, int dy) {
        this(dx, dy, true);
    }
}
