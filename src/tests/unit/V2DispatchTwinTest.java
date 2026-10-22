package tests.unit;

import atlantis.production.v2.Producible;
import atlantis.production.v2.ProductionItem;
import atlantis.production.v2.execution.DispatchResult;
import atlantis.production.v2.execution.V2DispatchTwin;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * V2 dispatch audit: every plan item the dispatcher acted on leaves a
 * queryable row (issued or refused, with the reason) - the V2 half of
 * the decision audit. A thin LIVE game then reads as few rows, not as
 * silence; a re-offer storm reads as many rows with the same tile.
 *
 * <p>No world, no engine: the twin is pure over dispatch results.</p>
 */
@Tag("infra")
public class V2DispatchTwinTest {

    private static final class FakeProducible implements Producible {
        private final String id;

        FakeProducible(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public atlantis.production.v2.ResourceCost cost() {
            return atlantis.production.v2.ResourceCost.of(100, 0, 2);
        }

        @Override
        public int buildDurationFrames() {
            return 100;
        }

        @Override
        public java.util.List<Producible> immediatePrerequisites() {
            return new ArrayList<Producible>();
        }

        @Override
        public String producerTypeId() {
            return "Probe";
        }

        @Override
        public boolean requiresPlacement() {
            return false;
        }
    }

    @BeforeEach
    public void clearLog() {
        EventLog.clearForTests();
    }

    @Test
    public void everyResultLeavesOneRowWithItsReason() {
        List<DispatchResult> results = new ArrayList<DispatchResult>();
        results.add(new DispatchResult(new ProductionItem(new FakeProducible("Pylon"), 100, false), true, "built@t1"));
        results.add(new DispatchResult(new ProductionItem(new FakeProducible("Zealot"), 120, false), false, "producer-busy"));
        results.add(null);

        V2DispatchTwin.emit(200, results);

        String log = EventLog.toJsonLines();
        assertEquals(2, countRows(log), "one row per real result, nulls skipped: " + log);
        assertTrue(log.contains("\"item\":\"Pylon\""), "item named: " + log);
        assertTrue(log.contains("\"start\":100"), "plan frame named: " + log);
        assertTrue(log.contains("\"issued\":true"), "accept named: " + log);
        assertTrue(log.contains("\"detail\":\"producer-busy\""), "refusal reason named: " + log);
    }

    @Test
    public void emptyAndNullEmitNothing() {
        V2DispatchTwin.emit(200, new ArrayList<DispatchResult>());
        V2DispatchTwin.emit(200, null);

        assertEquals(0, countRows(EventLog.toJsonLines()), "no results, no rows");
    }

    private static int countRows(String log) {
        int count = 0;
        int from = 0;
        while (true) {
            int at = log.indexOf("\"key\":\"V2_DISPATCH\"", from);
            if (at < 0) return count;

            count++;
            from = at + 1;
        }
    }
}
