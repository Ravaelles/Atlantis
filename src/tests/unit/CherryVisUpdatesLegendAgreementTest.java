package tests.unit;

import cherryvis.GameTrace;
import cherryvis.StringTable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code units_updates} carries manager ids, {@code custom_names.manager} is
 * the legend that resolves them - and both are rendered from the SAME runtime
 * strings table (one instance in the trace writer). If they ever diverge, the
 * viewer mislabels every manager line.
 *
 * <p>No CherryVis frontend is needed: feed both builders one shared table and
 * assert every id referenced by the updates resolves in the legend - in
 * runtime assignment order, not alphabetical, which is exactly the shape the
 * old hardcoded legend got wrong.</p>
 */
@Tag("infra")
public class CherryVisUpdatesLegendAgreementTest {

    @Test
    public void everyManagerIdInUpdatesResolvesInTheLegend() {
        GameTrace trace = new GameTrace();
        trace.logManager(100, 7, "ScoutTryFindingEnemy");
        trace.logManager(200, 7, "GatherResources");
        trace.logManager(150, 9, "GatherResources");

        StringTable strings = new StringTable();
        String updates = trace.buildUnitsUpdates(strings, true);
        String legend = GameTrace.customNames(strings);

        // Runtime order: Scout logged first, so it owns 10000.
        assertTrue(
            legend.contains("\"10000\":\"ScoutTryFindingEnemy\"")
                && legend.contains("\"10001\":\"GatherResources\""),
            "legend must follow runtime assignment order, was: " + legend);
        assertTrue(
            updates.contains("\"manager\":10000") && updates.contains("\"manager\":10001"),
            "updates must reference the same runtime ids, was: " + updates);
    }

    @Test
    public void everyTooltipIdInUpdatesResolvesInTheLegend() {
        // NEXT #221: tooltip rows share the manager legend table - a viewer
        // resolving one resolves the other, same runtime order.
        GameTrace trace = new GameTrace();
        trace.logTooltip(100, 9, "ClusterKite");

        StringTable strings = new StringTable();
        String tooltips = trace.buildTooltips(strings);
        String legend = GameTrace.customNames(strings);

        assertTrue(
            legend.contains("\"10000\":\"ClusterKite\""),
            "tooltip must resolve in the legend, was: " + legend);
        assertTrue(
            tooltips.contains("[100,10000]"),
            "updates must reference the tooltip id, was: " + tooltips);
    }
}
