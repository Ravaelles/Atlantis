package tests.unit;

import cherryvis.GameTrace;
import cherryvis.StringTable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Per-unit free-text logs ({@code units_logs}) were always empty - the viewer
 * had no per-unit narrative. Like the other CherryVis sections, the shape is
 * pinned as an exact string with no frontend running.
 *
 * <p>World-free: the trace takes primitives, so these tests pin the library
 * format directly, without FakeUnits for the format itself.</p>
 */
@Tag("infra")
public class CherryVisUnitLogsTest {

    @Test
    public void perUnitLinesRenderByUnitId() {
        GameTrace trace = new GameTrace();

        trace.logUnitLine(30, 2, "Build issued");
        trace.logUnitLine(10, 1, "Moving to site");
        trace.logUnitLine(40, 1, "Build refused");

        assertEquals(
            "\"1\":[{\"frame\":10,\"message\":\"Moving to site\"},"
                + "{\"frame\":40,\"message\":\"Build refused\"}],"
                + "\"2\":[{\"frame\":30,\"message\":\"Build issued\"}]",
            trace.buildUnitLogs()
        );
    }

    @Test
    public void tooltipRowsRenderChangeOnlyAsCompactPairs() {
        // NEXT #221, owner: grouped-by-key JSON, minimal info - one
        // [frame, tooltipId] pair per transition, change-only (the repeat
        // set at frame 20 lands once), managerless units included.
        GameTrace trace = new GameTrace();

        trace.logTooltip(10, 1, "ClusterForm");
        trace.logTooltip(20, 1, "ClusterForm");
        trace.logTooltip(30, 1, "ClusterKite");
        trace.logTooltip(15, 2, "Gatherer!");

        StringTable strings = new StringTable();
        String tooltips = trace.buildTooltips(strings);

        // Runtime string order: first-seen tooltips.
        assertEquals(
            "\"1\":[[10,10000],[30,10001]],"
                + "\"2\":[[15,10002]]",
            tooltips);
        assertTrue(strings.get("ClusterKite") == 10001,
            "tooltip ids come from the same runtime table as managers");
    }

    @Test
    public void tooltipChannelIsOffByDefault() {
        // Owner order: flapping callers wrote 114k rows / 4.2MB in one game,
        // so the channel rides CHERRYVIS_TOOLTIPS=1, never by default.
        assertFalse(atlantis.config.env.Env.cherryVisTooltips());
    }

    @Test
    public void unionRowsCarryTooltipNextToManagerWhenFlagged() {
        // The viewer reads units_updates: with the flag on, behavior tags
        // ride alongside managers (carry-forward), so an army unit with no
        // manager row still names what it does. Flag off: manager rows
        // only, byte-identical to before.
        boolean previous = atlantis.config.env.Env.cherryVisTooltips();
        atlantis.config.env.Env.setCherryVisTooltipsForTest(true);
        try {
            GameTrace trace = new GameTrace();

            trace.logManager(10, 1, "CombatManager");
            trace.logTooltip(10, 1, "ClusterForm");
            trace.logTooltip(20, 1, "ClusterKite");
            trace.logTooltip(15, 2, "Gatherer!");

            StringTable strings = new StringTable();
            String updates = trace.buildUnitsUpdates(strings, true);

            assertEquals(
                "\"1\":{\"10\":{\"manager\":10000,\"tooltip\":10001},"
                    + "\"20\":{\"tooltip\":10002}},"
                    + "\"2\":{\"15\":{\"tooltip\":10003}}",
                updates);
        } finally {
            atlantis.config.env.Env.setCherryVisTooltipsForTest(previous);
        }
    }

    @Test
    public void unitBehaviorSticksOnceDriven() {
        // Owner order: the unit holds its active behavior; drivers name it,
        // empty updates never erase it. The watcher keys on it, so a tagged
        // unit never goes bare again.
        FakeUnit unit = fake(atlantis.units.AUnitType.Protoss_Zealot, 10);

        assertEquals(null, unit.behavior());

        unit.setBehavior("ClusterKite");
        assertEquals("ClusterKite", unit.behavior());

        unit.setBehavior("");
        unit.setBehavior(null);
        assertEquals("ClusterKite", unit.behavior());

        unit.setTooltip("ClusterReengage", false);
        assertEquals("ClusterReengage", unit.behavior(),
            "order-funnel tags flow into the behavior field");
    }

    @Test
    public void unionRowsStayManagerOnlyWhenFlagOff() {
        GameTrace trace = new GameTrace();

        trace.logManager(10, 1, "CombatManager");
        trace.logTooltip(10, 1, "ClusterForm");

        StringTable strings = new StringTable();
        assertEquals(
            "\"1\":{\"10\":{\"manager\":10000}}",
            trace.buildUnitsUpdates(strings, false));
    }
}
