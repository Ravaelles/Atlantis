package tests.unit;

import atlantis.map.analysis.WallAnchorWarpTwin;
import atlantis.map.analysis.WallAnchors;
import atlantis.map.position.APosition;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * #97 M4 geometry gap: warped wall pieces must name their distance to the
 * anchor row and to the exact planned tile. Pure points, no world: the
 * recorder resolves the choke row, this pins the math and the row shape.
 */
@Tag("placement")
public class WallAnchorWarpTwinTest {

    private static List<APosition> row() {
        return WallAnchors.slots(
            APosition.createFromPixels(0, 320),
            APosition.createFromPixels(320, 320)).slots();
    }

    @BeforeEach
    public void clearLog() {
        EventLog.clearForTests();
    }

    @Test
    public void onAnchorReadsZero() {
        List<APosition> slots = row();

        assertEquals(0.0, WallAnchorWarpTwin.nearestSlotDist(slots, slots.get(3)),
            "a piece on its slot is walled");
    }

    @Test
    public void offAnchorReadsTiles() {
        List<APosition> slots = row();
        APosition away = APosition.create(slots.get(0).tx(), slots.get(0).ty() + 2);

        assertEquals(2.0, WallAnchorWarpTwin.nearestSlotDist(slots, away), 0.01,
            "two tiles off the row reads two tiles");
    }

    @Test
    public void noRowReadsSentinel() {
        assertEquals(-1.0, WallAnchorWarpTwin.nearestSlotDist(
            new ArrayList<APosition>(), APosition.create(10, 10)),
            "no anchor row is -1, not missing data");
        assertEquals(-1.0, WallAnchorWarpTwin.nearestSlotDist(row(), null),
            "no tile is -1, never an exception");
    }

    @Test
    public void emitNamesBothDistances() {
        List<APosition> slots = row();
        APosition warp = slots.get(4);
        APosition planned = APosition.create(warp.tx() + 1, warp.ty());

        WallAnchorWarpTwin.emit("Protoss_Pylon", warp, planned, true, slots, 100, "0:04");

        String log = EventLog.toJsonLines();
        assertTrue(log.contains("\"key\":\"WALL_ANCHOR_WARP\""), "row named: " + log);
        assertTrue(log.contains("\"anchorDist\":0"), "on the anchor: " + log);
        assertTrue(log.contains("\"plannedDist\":10"), "one tile off plan: " + log);
        assertTrue(log.contains("\"exact\":true"), "intent named: " + log);
    }

    @Test
    public void emitWithoutRowStillWitnesses() {
        WallAnchorWarpTwin.emit("Protoss_Gateway", APosition.create(50, 50),
            null, false, new ArrayList<APosition>(), 200, "0:08");

        String log = EventLog.toJsonLines();
        assertTrue(log.contains("\"anchorDist\":-1"), "sentinel, not silence: " + log);
        assertTrue(log.contains("\"plannedDist\":-1"), "no plan, sentinel: " + log);
    }
}
