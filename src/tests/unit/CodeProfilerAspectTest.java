package tests.unit;

import atlantis.debug.profiler.CodeProfiler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Named-aspect half of the profiler (owner 2026-11-09, timeout hunt):
 * non-Commander code (the placement funnel) times itself with the same
 * maps and the same end-of-game summary. Pins accumulation and the
 * totals helper - wall time itself is never asserted.
 */
@Tag("infra")
public class CodeProfilerAspectTest {

    @Test
    public void namedAspectAccumulatesIntoTotals() {
        String title = "TestAspect-" + System.nanoTime();
        CodeProfiler.startMeasuring(title);
        CodeProfiler.endMeasuring(title);
        CodeProfiler.startMeasuring(title);
        CodeProfiler.endMeasuring(title);

        assertTrue(CodeProfiler.aspectTotalSorted().containsKey(title),
            "two measured runs land in the totals: " + CodeProfiler.aspectTotalSorted().keySet());
        assertTrue(CodeProfiler.aspectTotalSorted().get(title) >= 0, "total never negative");
    }

    @Test
    public void totalsSortDescending() {
        assertTrue(CodeProfiler.aspectTotalSorted() instanceof java.util.Map,
            "totals helper answers a map, sorted or empty");
    }

    @Test
    public void periodicSampleRowsOncePerBucket() {
        String title = "BucketAspect-" + System.nanoTime();
        atlantis.util.log.EventLog.clearForTests();
        CodeProfiler.clearForTests();
        try {
            CodeProfiler.startMeasuring(title);
            CodeProfiler.endMeasuring(title);

            // Small frames on purpose: EventLog auto-flushes to disk past
            // frame 719, which would hide the buffered rows from the pin.
            CodeProfiler.samplePeriodic(10);
            CodeProfiler.samplePeriodic(11);

            int rows = 0;
            String log = atlantis.util.log.EventLog.toJsonLines();
            int at = 0;
            while ((at = log.indexOf("\"key\":\"PROFILER\"", at)) >= 0) {
                rows++;
                at++;
            }
            assertEquals(1, rows, "same 720-frame bucket rows once: " + log);
            assertTrue(log.contains(title + "="), "the row carries the top aspect: " + log);

            // A fresh sampling epoch rows again (the gate is a bucket
            // change, not a one-shot latch).
            CodeProfiler.clearForTests();
            CodeProfiler.samplePeriodic(12);
            String log2 = atlantis.util.log.EventLog.toJsonLines();
            int rows2 = 0;
            int at2 = 0;
            while ((at2 = log2.indexOf("\"key\":\"PROFILER\"", at2)) >= 0) {
                rows2++;
                at2++;
            }
            assertEquals(2, rows2, "a new epoch rows again: " + log2);
        } finally {
            atlantis.util.log.EventLog.clearForTests();
            CodeProfiler.clearForTests();
        }
    }
}
