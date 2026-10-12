package tests.unit;

import atlantis.cherryvis.simple.ACherryVisLogger_TraceJson;
import atlantis.cherryvis.simple.ACherryVis_Strings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The viewer resolves a unit's {@code manager} integer through
 * {@code custom_names.manager} - so the legend must be the runtime table,
 * not a hardcoded list.
 *
 * <p>The old code assigned ids at runtime from 10000 in first-logged order
 * while the legend hardcoded 10000=GatherResources, 10001=ScoutTryFindingEnemy
 * and so on: any game whose first-logged manager was not GatherResources
 * mislabeled every manager in the viewer, and managers past the hardcoded
 * table had no name at all. No CherryVis frontend is needed to prove it.</p>
 */
public class CherryVisCustomNamesTest {

    @Test
    public void legendFollowsRuntimeAssignmentOrder() {
        ACherryVis_Strings strings = new ACherryVis_Strings();
        strings.get("IdleWorker");
        strings.get("GatherResources");

        // NOTE: the leading space is the method's output, kept stable because
        // trace.json concatenates sections around it.
        assertEquals(
            " \"manager\": {\"10000\":\"IdleWorker\",\"10001\":\"GatherResources\"}",
            ACherryVisLogger_TraceJson.customNames(strings)
        );
    }
}
