package tests.unit;

import atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The per-unit half of CherryVis never worked: {@code units_first_seen} came
 * out malformed (single object instead of an array per frame, units lost when
 * two were discovered on the same frame) or crashed the trace write outright.
 *
 * <p>No CherryVis frontend is needed to prove it: the section is a pure
 * function of (frame, id, type, x, y) records, so the test pins the exact JSON
 * string the viewer parses.</p>
 */
public class CherryVisUnitFirstSeenTest {

    @Test
    public void twoUnitsOnOneFrameRenderAsArray() {
        ACherryVis_UnitFirstSeen.clear();
        ACherryVis_UnitFirstSeen.record(10, 246, 37, 3744, 3795);
        ACherryVis_UnitFirstSeen.record(10, 249, 35, 3525, 2864);
        ACherryVis_UnitFirstSeen.record(25, 251, 37, 3600, 2900);

        assertEquals(
            "\"10\":[{\"id\":246,\"type\":37,\"x\":3744,\"y\":3795},"
                + "{\"id\":249,\"type\":35,\"x\":3525,\"y\":2864}],"
                + "\"25\":[{\"id\":251,\"type\":37,\"x\":3600,\"y\":2900}]",
            ACherryVis_UnitFirstSeen.get()
        );
    }

    @Test
    public void frameZeroBecomesOne() {
        ACherryVis_UnitFirstSeen.clear();
        ACherryVis_UnitFirstSeen.record(0, 246, 37, 3744, 3795);

        assertEquals(
            "\"1\":[{\"id\":246,\"type\":37,\"x\":3744,\"y\":3795}]",
            ACherryVis_UnitFirstSeen.get()
        );
    }

    @Test
    public void sameUnitRecordedTwiceAppearsOnce() {        ACherryVis_UnitFirstSeen.clear();
        ACherryVis_UnitFirstSeen.record(10, 246, 37, 3744, 3795);
        ACherryVis_UnitFirstSeen.record(11, 246, 37, 3744, 3795);

        assertEquals(
            "\"10\":[{\"id\":246,\"type\":37,\"x\":3744,\"y\":3795}]",
            ACherryVis_UnitFirstSeen.get()
        );
    }
}
