package tests.unit;

import atlantis.util.AConsole;
import atlantis.util.log.LogLevel;
import atlantis.util.log.LogPort;
import atlantis.util.log.SystemLogPort;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("infra")
public class AConsoleTest {

    /**
     * The port a fresh JVM uses, captured before any test swaps it.
     */
    private static LogPort originalPort;

    private RecordingLogPort recording;

    @BeforeAll
    public static void captureOriginalPort() {
        originalPort = AConsole.port();
    }

    @BeforeEach
    public void setUp() {
        recording = new RecordingLogPort();
        AConsole.usePort(recording);
    }

    @AfterEach
    public void tearDown() {
        AConsole.usePort(originalPort);
    }

    @Test
    public void consoleIsThePortUntilSomethingReplacesIt() {
        assertInstanceOf(SystemLogPort.class, originalPort, "a fresh JVM must write to the console, not to a test double");
    }

    @Test
    public void messagesGoToTheInjectedPort() {
        AConsole.println("hello");
        AConsole.errPrintln("bad");
        AConsole.print("no newline");
        AConsole.println();

        assertEquals(Arrays.asList("[MED] hello", "no newline", ""), recording.out);
        assertEquals(Collections.singletonList("[MED] bad"), recording.errors);
    }

    @Test
    public void explicitLevelOverridesTheDefault() {
        AConsole.println(LogLevel.HIGH, "churn");
        AConsole.println(LogLevel.CRITICAL, "stuck");
        AConsole.println(LogLevel.LOW, "flow");
        AConsole.errPrintln(LogLevel.CRITICAL, "dead");

        assertEquals(
            Arrays.asList("[HIGH] churn", "[CRIT] stuck", "[LOW] flow"),
            recording.out
        );
        assertEquals(Collections.singletonList("[CRIT] dead"), recording.errors);
    }

    @Test
    public void tempMarksRemovableDebuggingLines() {
        AConsole.temp("probe xyz");

        assertEquals(Collections.singletonList("[TEMP] probe xyz"), recording.out);
    }

    @Test
    public void stackTracesGoThroughThePort() {
        AConsole.printStackTrace("boom");

        assertEquals(Collections.singletonList("boom"), recording.stackTraces);
        assertTrue(recording.errors.isEmpty(), "the port decides how a stack is rendered");
    }

    @Test
    public void varargsPrintJoinsWithCommas() {
        AConsole.print("a", "b", "c");

        assertEquals(Arrays.asList("a", ", ", "b, ", "[MED] c"), recording.out);
    }

    @Test
    public void printListFormatsThroughThePort() {
        AConsole.printList(Arrays.asList("x", "y"));

        assertEquals(Arrays.asList("[MED] List (2)", "[MED] - x", "[MED] - y"), recording.out);
    }

    @Test
    public void convertStackToStringKeepsTheFirstLines() {
        StackTraceElement[] stack = {
            new StackTraceElement("A", "m1", "A.java", 1),
            new StackTraceElement("B", "m2", "B.java", 2),
            new StackTraceElement("C", "m3", "C.java", 3),
        };

        assertEquals(2, AConsole.convertStackToString(2, stack).split("\n").length);
        assertTrue(AConsole.convertStackToString(2, stack).startsWith("A.m1(A.java:1)"));
        assertTrue(AConsole.convertStackToString(stack).startsWith("A.m1(A.java:1)"));
    }

    @Test
    public void nullPortRestoresTheConsole() {
        AConsole.usePort(null);

        assertInstanceOf(SystemLogPort.class, AConsole.port(), "null restores the console");
    }

    /**
     * Test double: records what the console was asked to write, so tests assert
     * on output instead of scraping {@code System.out}.
     */
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
