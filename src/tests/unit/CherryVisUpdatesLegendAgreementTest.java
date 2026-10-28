package tests.unit;

import atlantis.cherryvis.simple.ACherryVisLogger_TraceJson;
import atlantis.cherryvis.simple.ACherryVisUnitLogger;
import atlantis.cherryvis.simple.ACherryVis_Strings;
import atlantis.cherryvis.simple.ACherryVis_UnitLogs;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

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
        ACherryVisUnitLogger logger = new ACherryVisUnitLogger();
        logger.unitsManagerLogs.computeIfAbsent(7, k -> new TreeMap<>()).put(100, "ScoutTryFindingEnemy");
        logger.unitsManagerLogs.computeIfAbsent(7, k -> new TreeMap<>()).put(200, "GatherResources");
        logger.unitsManagerLogs.computeIfAbsent(9, k -> new TreeMap<>()).put(150, "GatherResources");

        ACherryVis_Strings strings = new ACherryVis_Strings();
        String updates = ACherryVis_UnitLogs.build(logger, strings);
        String legend = ACherryVisLogger_TraceJson.customNames(strings);

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
        ACherryVisUnitLogger logger = new ACherryVisUnitLogger();
        tests.fakes.FakeUnit zealot = tests.unit.AbstractTestWithUnits.fake(
            atlantis.units.AUnitType.Protoss_Zealot, 10);
        logger.tooltip(100, "ClusterKite", zealot);

        ACherryVis_Strings strings = new ACherryVis_Strings();
        String updates = ACherryVis_UnitLogs.build(logger, strings);
        String legend = ACherryVisLogger_TraceJson.customNames(strings);

        assertTrue(
            legend.contains("\"10000\":\"ClusterKite\""),
            "tooltip must resolve in the legend, was: " + legend);
        assertTrue(
            updates.contains("\"tooltip\":10000"),
            "updates must reference the tooltip id, was: " + updates);
    }
}
