package tests.unit;

import atlantis.production.producibles.Producible;
import atlantis.production.scheduling.ProductionItem;
import atlantis.production.execution.DispatchResult;
import atlantis.production.execution.V2DispatchTwin;
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

    @BeforeEach
    public void clearLog() {
        EventLog.clearForTests();
        V2DispatchTwin.clearForTests();
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
        public atlantis.production.producibles.ResourceCost cost() {
            return atlantis.production.producibles.ResourceCost.of(100, 0, 2);
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

    @Test
    public void identicalReoffersHeartbeatInsteadOfSpamming() {
        List<DispatchResult> results = new ArrayList<DispatchResult>();
        results.add(new DispatchResult(new ProductionItem(new FakeProducible("Citadelo"), 100, false), false, "no placement"));

        V2DispatchTwin.emit(100, results);
        V2DispatchTwin.emit(101, results);
        V2DispatchTwin.emit(200, results);

        assertEquals(1, countRows(EventLog.toJsonLines()),
            "same item, verdict, reason and start inside the window: one row, not three");
    }

    @Test
    public void slidingStartStillHeartbeats() {
        List<DispatchResult> at100 = new ArrayList<DispatchResult>();
        at100.add(new DispatchResult(new ProductionItem(new FakeProducible("Citadelo"), 100, false), false, "no placement"));

        List<DispatchResult> at101 = new ArrayList<DispatchResult>();
        at101.add(new DispatchResult(new ProductionItem(new FakeProducible("Citadelo"), 101, false), false, "no placement"));

        V2DispatchTwin.emit(100, at100);
        V2DispatchTwin.emit(101, at101);

        assertEquals(1, countRows(EventLog.toJsonLines()),
            "the scheduler re-plans every frame, so a stuck demand slides its start: "
                + "the start is plan trivia, not news - one row, not two");
    }

    @Test
    public void changedVerdictRowsImmediately() {
        List<DispatchResult> results = new ArrayList<DispatchResult>();
        results.add(new DispatchResult(new ProductionItem(new FakeProducible("Citadelo"), 100, false), false, "no placement"));

        List<DispatchResult> changed = new ArrayList<DispatchResult>();
        changed.add(new DispatchResult(new ProductionItem(new FakeProducible("Citadelo"), 100, true), true, "builder committed"));

        V2DispatchTwin.emit(100, results);
        V2DispatchTwin.emit(101, changed);

        assertEquals(2, countRows(EventLog.toJsonLines()),
            "a changed verdict is news, not a repeat");
    }

    @Test
    public void heartbeatRepeatsAfterTheWindow() {
        List<DispatchResult> results = new ArrayList<DispatchResult>();
        results.add(new DispatchResult(new ProductionItem(new FakeProducible("Citadelo"), 100, false), false, "no placement"));

        V2DispatchTwin.emit(100, results);
        V2DispatchTwin.emit(100 + V2DispatchTwin.HEARTBEAT_FRAMES, results);

        assertEquals(2, countRows(EventLog.toJsonLines()),
            "the heartbeat row proves the demand is still alive");
    }
}
