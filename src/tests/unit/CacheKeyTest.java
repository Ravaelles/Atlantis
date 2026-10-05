package tests.unit;

import atlantis.units.select.CacheKey;
import atlantis.util.AConsole;
import atlantis.util.GameClock;
import atlantis.util.log.LogPort;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * `construction.id()` is an {@code int}, and two position-finder call sites pass it
 * into the key ({@code APositionFinder:70}, {@code SupplyDepotPositionFinder:26}) - ever
 * since the `Construction` branch was removed when this class moved beside
 * `Selection`. Without an {@code Integer} branch every new-building position search in
 * a real game logged "Unknown object to CacheKey: java.lang.Integer" with a stack
 * trace (reported 2026-10-05 from a game on the script-built jar).
 *
 * <p>The fallback already returned {@code object.toString()}, which for an
 * {@code Integer} is the decimal string - so this branch changes no key anywhere, it
 * only stops the error. Both halves are pinned: the format (identical to what the
 * fallback produced) and the silence (nothing reaches the log port).</p>
 *
 * <p>The silence half swaps the log port for a recording one - the house pattern from
 * {@code AConsoleTest} - and publishes a far-future clock first, so the once-a-minute
 * throttle cannot suppress the error on account of an earlier test in the same JVM
 * having triggered it. Both are restored afterwards.</p>
 */
public class CacheKeyTest {

    @Test
    public void integerFormatsAsItsDecimalString() {
        assertEquals("172", CacheKey.toKey(172),
            "the fallback produced this exact string, so no cache key changes");
    }

    @Test
    public void thePositionFinderKeyShapeContainsTheConstructionId() {
        String key = CacheKey.create("findPositionForNew", "Protoss_Nexus", "nearTo", 172, "35.0");

        assertTrue(key.contains("172"),
            "the construction id is what tells two pending bases apart in the key");
    }

    @Test
    public void anIntegerKeyLogsNothing() {
        RecordingLogPort recording = new RecordingLogPort();
        LogPort previous = currentPort();
        int framesBefore = GameClock.frames();
        int secondsBefore = GameClock.seconds();

        try {
            AConsole.usePort(recording);
            GameClock.publish(framesBefore + 100000, secondsBefore + 100000);

            CacheKey.toKey(172);
            CacheKey.create("findPosition", "builder", 172, "nearTo");

            assertTrue(recording.errors.isEmpty() && recording.stackTraces.isEmpty(),
                "an Integer is whitelisted - nothing may reach the error log, got: "
                    + recording.errors + recording.stackTraces);
        }
        finally {
            AConsole.usePort(previous);
            GameClock.publish(framesBefore, secondsBefore);
        }
    }

    @Test
    public void existingBranchesAreUntouched() {
        assertEquals("NuLL", CacheKey.toKey(null));
        assertEquals("Nexus", CacheKey.toKey("Nexus"));
        assertEquals("35.0", CacheKey.toKey(35.04));
    }

    // =========================================================

    private static LogPort currentPort() {
        try {
            java.lang.reflect.Field field = AConsole.class.getDeclaredField("port");
            field.setAccessible(true);
            return (LogPort) field.get(null);
        }
        catch (ReflectiveOperationException e) {
            throw new IllegalStateException("AConsole.port field moved", e);
        }
    }

    private static class RecordingLogPort implements LogPort {
        private final List<String> out = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private final List<String> stackTraces = new ArrayList<>();

        @Override
        public void print(Object message) {
            out.add(String.valueOf(message));
        }

        @Override
        public void println(Object message) {
            out.add(String.valueOf(message));
        }

        @Override
        public void printError(Object message) {
            errors.add(String.valueOf(message));
        }

        @Override
        public void printStackTrace(String message) {
            stackTraces.add(String.valueOf(message));
        }
    }
}