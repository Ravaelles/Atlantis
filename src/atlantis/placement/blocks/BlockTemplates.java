package atlantis.placement.blocks;

import atlantis.placement.core.BuildBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * The template table (`_AI/redesign/03_PLACEMENT.md` §1.1, §2 Step 3.1).
 *
 * <p>
 * Stardust ships 24 hand-written C++ Block classes plus 10 start-block
 * variants.
 * Here the same set is <b>data</b>: each row is a {@link BuildBlock.Spec}, so
 * the
 * whole catalogue is one table and a new template is one row. The geometry is
 * ported from Stardust's {@code src/Builder/Blocks/*.h} (widths, heights, Pylon
 * offsets and slot layouts).
 * </p>
 *
 * <p>
 * The list is ordered largest-first, the order Stardust scans, so a big block
 * gets
 * the interior before smaller ones fill the gaps.
 * </p>
 */
public final class BlockTemplates {

    private BlockTemplates() {
    }

    /** Every normal (non-start) template, largest first. */
    public static List<BuildBlock.Spec> normal() {
        List<BuildBlock.Spec> specs = new ArrayList<>();

        specs.add(spec("Block18x6", 18, 6, 9, 2,
                cells(0, 2, 2, 2, 4, 2, 12, 2, 14, 2, 16, 2),
                cells(6, 0, 9, 0), cells(), cells(0, 0, 12, 2)));

        specs.add(spec("Block16x8", 16, 8, 8, 3,
                cells(2, 4, 4, 4, 9, 0, 11, 0, 0, 4, 0, 6),
                cells(13, 0, 13, 3), cells(), cells(0, 0, 5, 0, 9, 3, 0, 5)));

        specs.add(spec("Block17x6", 17, 6, 8, 2,
                cells(2, 2, 4, 2, 12, 2, 14, 2, 0, 0, 0, 4),
                cells(6, 0, 9, 3), cells(), cells(11, 1, 2, 3)));

        specs.add(spec("Block14x6", 14, 6, 7, 2,
                cells(2, 2, 11, 2, 4, 0, 4, 4),
                cells(0, 2, 6, 0), cells(), cells(2, 3, 10, 3)));

        specs.add(spec("Block12x8", 12, 8, 5, 3,
                cells(8, 3, 10, 3, 0, 2, 0, 5),
                cells(5, 5, 8, 0), cells(), cells(0, 0, 8, 5)));

        specs.add(spec("Block16x5", 16, 5, 7, 2,
                cells(2, 2, 4, 2, 10, 2, 12, 2, 0, 2),
                cells(6, 2, 8, 0), cells(), cells(13, 1, 2, 2)));

        specs.add(spec("Block18x3", 18, 3, 8, 1,
                cells(2, 1, 4, 1, 12, 1, 14, 1, 0, 0),
                cells(6, 1, 10, 1), cells(), cells()));

        specs.add(spec("Block13x6", 13, 6, 5, 2,
                cells(2, 2, 10, 2, 2, 0, 10, 4),
                cells(7, 1, 7, 3), cells(), cells(0, 2, 9, 2)));

        specs.add(spec("Block10x6", 10, 6, 4, 2,
                cells(2, 2, 7, 2, 0, 2, 6, 4),
                cells(6, 1, 6, 3), cells(), cells(6, 0)));

        specs.add(spec("Block8x8", 8, 8, 3, 3,
                cells(1, 3, 5, 3, 3, 1, 3, 5, 0, 1, 0, 5),
                cells(0, 3, 5, 3), cells(), cells(0, 0, 4, 0)));

        specs.add(spec("Block14x3", 14, 3, 6, 1,
                cells(2, 1, 10, 1, 0, 1, 12, 1),
                cells(4, 1, 8, 1), cells(), cells()));

        specs.add(spec("Block12x5", 12, 5, 5, 2,
                cells(2, 2, 8, 2, 0, 2, 10, 2),
                cells(6, 0, 6, 3), cells(), cells()));

        specs.add(spec("Block10x3", 10, 3, 4, 1,
                cells(2, 1, 6, 1, 0, 1, 8, 1),
                cells(4, 1), cells(), cells()));

        specs.add(spec("Block8x5", 8, 5, 3, 2,
                cells(0, 2, 6, 2, 2, 0, 2, 3),
                cells(5, 1), cells(), cells(2, 2)));

        specs.add(spec("Block4x8", 4, 8, 1, 3,
                cells(0, 1, 0, 3, 0, 5, 2, 3),
                cells(0, 0, 0, 6), cells(), cells()));

        specs.add(spec("Block6x3", 6, 3, 2, 1,
                cells(0, 1, 4, 1, 2, 0),
                cells(2, 1), cells(), cells()));

        specs.add(spec("Block4x5", 4, 5, 1, 2,
                cells(0, 0, 0, 3, 2, 2),
                cells(0, 2), cells(), cells()));

        specs.add(spec("Block8x2", 8, 2, 3, 0,
                cells(0, 0, 6, 0, 2, 0),
                cells(2, 0), cells(), cells()));

        specs.add(spec("Block5x4", 5, 4, 2, 1,
                cells(0, 0, 3, 0, 0, 2, 3, 2),
                cells(), cells(0, 1, 3, 1), cells()));

        specs.add(spec("Block5x2", 5, 2, 2, 0,
                cells(0, 0, 3, 0),
                cells(), cells(), cells()));

        specs.add(spec("Block4x4", 4, 4, 1, 1,
                cells(0, 0, 2, 2),
                cells(), cells(0, 2, 2, 0), cells()));

        specs.add(spec("Block4x2", 4, 2, 1, 0,
                cells(0, 0, 2, 0),
                cells(), cells(), cells()));

        specs.add(spec("Block2x4", 2, 4, 0, 1,
                cells(0, 0, 0, 2),
                cells(), cells(), cells()));

        specs.add(spec("Block2x2", 2, 2, 0, 0,
                cells(0, 0),
                cells(), cells(), cells()));

        return specs;
    }

    /**
     * The start-block variants, in Stardust's priority order
     * (`_AI/redesign/03_PLACEMENT.md` §2 Step 1.1). A start block is the anchor
     * layout for a base: ~2 gateways, 2-4 tech buildings, 3-4 cannons.
     *
     * <p>
     * Four variants ship here (normal left/right, compact left/right); the
     * remaining
     * six are edge cases for unusual mains and are added the same way - a row.
     * </p>
     */
    public static List<BuildBlock.Spec> startBlocks() {
        List<BuildBlock.Spec> specs = new ArrayList<>();

        specs.add(spec("StartNormalLeft", 12, 8, 5, 3,
                cells(8, 3, 10, 3, 0, 2, 0, 5, 8, 5),
                cells(5, 5, 8, 0), cells(0, 0, 3, 0), cells(6, 0, 0, 0)));

        specs.add(spec("StartNormalRight", 12, 8, 6, 3,
                cells(1, 3, 3, 3, 1, 2, 11, 2, 1, 5),
                cells(6, 5, 3, 0), cells(8, 0, 11, 0), cells(5, 0, 11, 0)));

        specs.add(spec("StartCompactLeft", 10, 8, 4, 3,
                cells(6, 3, 2, 2, 2, 5, 0, 2, 0, 5),
                cells(4, 5, 6, 0), cells(0, 0, 2, 0), cells(4, 0, 0, 0)));

        specs.add(spec("StartCompactRight", 10, 8, 5, 3,
                cells(1, 3, 3, 3, 7, 2, 0, 2, 9, 2),
                cells(5, 5, 3, 0), cells(7, 0, 9, 0), cells(5, 0, 9, 0)));

        return specs;
    }

    private static BuildBlock.Spec spec(
            String name, int width, int height, int pylonDx, int pylonDy,
            int[][] small, int[][] medium, int[][] mediumNoExit, int[][] large) {
        return new BuildBlock.Spec(name, width, height, pylonDx, pylonDy,
                small, medium, mediumNoExit, large);
    }

    /** {@code cells(x1,y1, x2,y2, ...)} - a readable literal for a slot list. */
    private static int[][] cells(int... xy) {
        int[][] offsets = new int[xy.length / 2][2];
        for (int i = 0; i < offsets.length; i++) {
            offsets[i][0] = xy[i * 2];
            offsets[i][1] = xy[i * 2 + 1];
        }
        return offsets;
    }
}
