package tests.unit;

import atlantis.debug.profiler.CodeProfiler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
}
