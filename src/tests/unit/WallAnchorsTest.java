package tests.unit;

import atlantis.map.analysis.WallAnchors;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * M4 model slice: wall anchors are the tile row along the choke segment,
 * door in the middle - pure geometry, no game. A 10-tile straight choke
 * pins contiguity, order and the door; a point choke pins the degenerate
 * case.
 */
@Tag("placement")
public class WallAnchorsTest {

    @Test
    public void straightChokeYieldsAnOrderedRow() {
        WallAnchors.AnchorRow row = WallAnchors.slots(
            APosition.createFromPixels(0, 320),
            APosition.createFromPixels(320, 320));

        assertTrue(row.slots().size() >= 9 && row.slots().size() <= 11);
        assertEquals(0, row.slots().get(0).tx());
        assertEquals(10, row.slots().get(row.slots().size() - 1).tx());
        for (int i = 1; i < row.slots().size(); i++) {
            assertTrue(
                Math.abs(row.slots().get(i).tx() - row.slots().get(i - 1).tx()) <= 1
                    && Math.abs(row.slots().get(i).ty() - row.slots().get(i - 1).ty()) <= 1,
                "anchors walk the segment without jumps");
        }
    }

    @Test
    public void doorIsTheMiddleSlot() {
        WallAnchors.AnchorRow row = WallAnchors.slots(
            APosition.createFromPixels(0, 320),
            APosition.createFromPixels(320, 320));

        assertEquals(row.slots().get(row.slots().size() / 2), row.door());
    }

    @Test
    public void pointChokeYieldsItsOwnTile() {
        WallAnchors.AnchorRow row = WallAnchors.slots(
            APosition.createFromPixels(160, 320),
            APosition.createFromPixels(160, 320));

        assertEquals(1, row.slots().size());
        assertEquals(row.slots().get(0), row.door());
    }

    @Test
    public void nullEndsYieldNoRow() {
        WallAnchors.AnchorRow row = WallAnchors.slots(null, null);

        assertEquals(0, row.slots().size());
    }
}
