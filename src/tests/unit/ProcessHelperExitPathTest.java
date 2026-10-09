package tests.unit;

import atlantis.util.ProcessHelper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The game-exit path must not dump stack traces for expected, unactionable
 * failures.
 *
 * <p>
 * Report (owner, 2026-10-09): a run that ended through
 * {@code ForceExitLocallyAfterRealSeconds} (started from the IDE) printed
 *
 * <pre>
 * Killing Wine game processes...
 * java.io.IOException: Cannot run program "pkill": CreateProcess error=2, File not found
 *     at java.lang.ProcessBuilder.start(ProcessBuilder.java:1048)
 *     ...
 *     at atlantis.util.ProcessHelper.executeInCommandLine(ProcessHelper.java:338)
 * </pre>
 *
 * The exit path is exactly where a failed kill is expected - on Linux there is
 * no {@code taskkill}, and from the IDE the JVM cannot even name the Wine
 * processes - and at that point the outcome no longer matters: the match is
 * over and the process is exiting. A stack trace there trains the reader to
 * ignore console output, which is what this class pins away.
 * </p>
 *
 * <p>
 * The check is behavioural rather than textual: capture {@code System.err} and
 * assert the kill helpers write at most a one-line note, never a
 * {@code java.io.IOException} header or a {@code \tat } frame.
 * </p>
 */
public class ProcessHelperExitPathTest {

    private String stderrOf(Runnable body) {
        PrintStream original = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured));
        try {
            body.run();
        } finally {
            System.setErr(original);
        }
        return captured.toString();
    }

    private void assertNoStackDump(String label, String stderr) {
        assertFalse(stderr.contains("java.io.IOException"),
                label + " must not print an IOException header:\n" + stderr);
        assertFalse(stderr.contains("\tat "),
                label + " must not print a stack frame:\n" + stderr);
        assertFalse(stderr.contains("Exception in thread"),
                label + " must not print an uncaught-exception banner:\n" + stderr);
    }

    @Test
    public void openBwExitCleanupDoesNotDumpStackTraces() {
        String stderr = stderrOf(new Runnable() {
            @Override
            public void run() {
                ProcessHelper.killOpenBWProcesses();
            }
        });

        assertNoStackDump("the OpenBW exit cleanup", stderr);
    }

    @Test
    public void wineHostKillDoesNotDumpStackTraces() {
        // killWineHostProcesses already routes through sh -c, which swallows a
        // missing pkill; the point is that even its failure mode stays a one-liner.
        String stderr = stderrOf(new Runnable() {
            @Override
            public void run() {
                ProcessHelper.killWineHostProcesses();
            }
        });

        assertNoStackDump("the Wine host kill", stderr);
    }

    /**
     * The process helpers must never print more than a single-line,
     * human-readable note. This bounds the noise on the exit path as a whole:
     * whatever the OS does or does not have, the console gets at most one line
     * per failed command, and never a Java trace.
     */
    @Test
    public void aFailedKillIsAtMostOneLineOfNote() {
        String stderr = stderrOf(new Runnable() {
            @Override
            public void run() {
                ProcessHelper.killOpenBWProcesses();
            }
        });

        int newlines = 0;
        for (int i = 0; i < stderr.length(); i++) {
            if (stderr.charAt(i) == '\n')
                newlines++;
        }
        assertTrue(newlines <= 4,
                "expected at most a few one-line notes, got " + newlines + " lines:\n" + stderr);
    }
}
