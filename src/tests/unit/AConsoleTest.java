package tests.unit;

import atlantis.util.AConsole;
import atlantis.util.log.LogPort;
import atlantis.util.log.SystemLogPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(originalPort instanceof SystemLogPort,
            "a fresh JVM must write to the console, not to a test double");
    }

    @Test
    public void messagesGoToTheInjectedPort() {
        AConsole.println("hello");
        AConsole.errPrintln("bad");
        AConsole.print("no newline");
        AConsole.println();

        assertEquals(Arrays.asList("hello", "no newline", ""), recording.out);
        assertEquals(Arrays.asList("bad"), recording.errors);
    }

    @Test
    public void stackTracesGoThroughThePort() {
        AConsole.printStackTrace("boom");

        assertEquals(Arrays.asList("boom"), recording.stackTraces);
        assertTrue(recording.errors.isEmpty(), "the port decides how a stack is rendered");
    }

    @Test
    public void varargsPrintJoinsWithCommas() {
        AConsole.print("a", "b", "c");

        assertEquals(Arrays.asList("a", ", ", "b, ", "c"), recording.out);
    }

    @Test
    public void printListFormatsThroughThePort() {
        AConsole.printList(Arrays.asList("x", "y"));

        assertEquals(Arrays.asList("List (2)", "- x", "- y"), recording.out);
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

        assertTrue(AConsole.port() instanceof SystemLogPort, "null restores the console");
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
