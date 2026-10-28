package tests.unit;

import atlantis.cherryvis.simple.ACherryVisUnitLogger;
import atlantis.cherryvis.simple.ACherryVis_Strings;
import atlantis.cherryvis.simple.ACherryVis_UnitLogs;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    public void tooltipRowsRenderChangeOnlyWithCarryForward() {
        // NEXT #221: the Task slot feed. Repeat sets of the same tag land
        // once; a unit with no manager row anywhere still lands (that is
        // the army gap - 10 of 26 combat units had no units_updates entry).
        ACherryVisUnitLogger logger = new ACherryVisUnitLogger();
        FakeUnit first = fake(AUnitType.Protoss_Zealot, 10);
        FakeUnit second = fake(AUnitType.Protoss_Probe, 12);

        logger.tooltip(10, "ClusterForm", first);
        logger.tooltip(20, "ClusterForm", first);
        logger.tooltip(30, "ClusterKite", first);
        logger.tooltip(15, "Gatherer!", second);

        ACherryVis_Strings strings = new ACherryVis_Strings();
        String updates = ACherryVis_UnitLogs.build(logger, strings);

        // Runtime string order: first-seen tooltips, managers untouched.
        // Manager rows carry the carried-forward tooltip; the managerless
        // unit still renders its tooltip row.
        assertEquals(
            "\"" + first.id() + "\":{\"10\":{\"tooltip\":10000},"
                + "\"30\":{\"tooltip\":10001}},"
                + "\"" + second.id() + "\":{\"15\":{\"tooltip\":10002}}",
            updates
        );
        assertTrue(strings.get("ClusterKite") == 10001,
            "tooltip ids come from the same runtime table as managers");
    }
}
