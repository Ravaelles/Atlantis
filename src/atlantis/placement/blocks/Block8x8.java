package atlantis.placement.blocks;

import atlantis.placement.core.BuildBlock;
import atlantis.placement.core.Slot;

import java.util.Arrays;
import java.util.List;

/**
 * An 8x8 Protoss block: 4 large (4x3), 2 medium (3x2) and a Pylon that powers
 * the
 * middle, ported from Stardust's {@code Block8x8}.
 *
 * <p>
 * The workhorse normal block - it fits most mains and offers the tech-building
 * slots a base actually needs. Other templates follow the same shape: subclass
 * {@link BuildBlock}, answer the geometry, done.
 * </p>
 */
public final class Block8x8 extends BuildBlock {

    private static final List<Slot> SMALL = Arrays.asList(
            new Slot(3, 3),
            new Slot(1, 3), new Slot(5, 3),
            new Slot(0, 1), new Slot(2, 1), new Slot(4, 1), new Slot(6, 1),
            new Slot(0, 5), new Slot(2, 5), new Slot(4, 5), new Slot(6, 5));

    private static final List<Slot> MEDIUM = Arrays.asList(
            new Slot(0, 3), new Slot(5, 3));

    private static final List<Slot> LARGE = Arrays.asList(
            new Slot(0, 0), new Slot(0, 5), new Slot(4, 0), new Slot(4, 5));

    public Block8x8(int left, int top) {
        super(left, top);
    }

    @Override
    public int width() {
        return 8;
    }

    @Override
    public int height() {
        return 8;
    }

    @Override
    protected int powerPylonDx() {
        return 3;
    }

    @Override
    protected int powerPylonDy() {
        return 3;
    }

    @Override
    protected List<Slot> smallSlots() {
        return SMALL;
    }

    @Override
    protected List<Slot> mediumSlots() {
        return MEDIUM;
    }

    @Override
    protected List<Slot> largeSlots() {
        return LARGE;
    }
}
