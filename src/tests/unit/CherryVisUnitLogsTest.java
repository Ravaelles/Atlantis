package tests.unit;

import atlantis.cherryvis.simple.ACherryVisUnitLogger;
import atlantis.cherryvis.simple.ACherryVis_UnitLogs;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Per-unit free-text logs ({@code units_logs}) were always empty - the viewer
 * had no per-unit narrative. Like the other CherryVis sections, the shape is
 * pinned as an exact string with no frontend running.
 */
@Tag("infra")
public class CherryVisUnitLogsTest {

    @Test
    public void perUnitLinesRenderByUnitId() {
        ACherryVisUnitLogger logger = new ACherryVisUnitLogger();
        FakeUnit first = fake(AUnitType.Protoss_Probe, 10);
        FakeUnit second = fake(AUnitType.Protoss_Probe, 12);

        logger.log(30, "Build issued", second);
        logger.log(10, "Moving to site", first);
        logger.log(40, "Build refused", first);

        assertEquals(
            "\"" + first.id() + "\":[{\"frame\":10,\"message\":\"Moving to site\"},"
                + "{\"frame\":40,\"message\":\"Build refused\"}],"
                + "\"" + second.id() + "\":[{\"frame\":30,\"message\":\"Build issued\"}]",
            ACherryVis_UnitLogs.buildUnitLogs(logger)
        );
    }
}
