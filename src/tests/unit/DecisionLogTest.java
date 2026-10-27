package tests.unit;

import atlantis.decisions.board.ADecision;
import atlantis.decisions.board.DecisionLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A run owns its decisions file: the first flush of a JVM run truncates
 * whatever a previous run left behind, later flushes append. Measured
 * 2026-10-17: per-run E2E_DECISIONS carried rows from all previous games
 * because the writer only ever appended.
 *
 * <p>The test runs against the real output path with a backup/restore
 * around it, so it proves the production path (record -> flush -> file)
 * instead of a seam.</p>
 */
@Tag("infra")
public class DecisionLogTest {

    private byte[] backup;
    private boolean hadFile;

    private static ADecision build(String subject, int frame) {
        return ADecision.decide(
                ADecision.Kind.BUILD, subject, "At 8 Pylon", ADecision.Source.BUILD_ORDER)
            .frame(frame).supply(8, 9).bank(50, 0)
            .build();
    }

    private static String content() throws Exception {
        return new String(Files.readAllBytes(new File(DecisionLog.OUTPUT_PATH).toPath()), StandardCharsets.UTF_8);
    }

    @BeforeEach
    public void backupAndClean() throws Exception {
        File out = new File(DecisionLog.OUTPUT_PATH);
        hadFile = out.exists();
        backup = hadFile ? Files.readAllBytes(out.toPath()) : null;
        DecisionLog.clearForTests();
    }

    @AfterEach
    public void restore() throws Exception {
        DecisionLog.clearForTests();
        File out = new File(DecisionLog.OUTPUT_PATH);
        if (hadFile) {
            Files.write(out.toPath(), backup);
        }
        else if (out.exists()) {
            out.delete();
        }
    }

    @Test
    public void firstFlushTruncatesStaleRunSecondAppends() throws Exception {
        File out = new File(DecisionLog.OUTPUT_PATH);
        if (out.getParentFile() != null) out.getParentFile().mkdirs();
        Files.write(out.toPath(), "STALE ROW FROM A PREVIOUS GAME\n".getBytes(StandardCharsets.UTF_8));

        DecisionLog.record(build("Pylon", 100));
        assertTrue(content().contains("\"subject\":\"Pylon\""));
        assertFalse(content().contains("STALE ROW"));

        DecisionLog.record(build("Gateway", 200));
        String both = content();
        assertTrue(both.contains("\"subject\":\"Pylon\""));
        assertTrue(both.contains("\"subject\":\"Gateway\""));
        assertFalse(both.contains("STALE ROW"));
    }

    @Test
    public void simulatedRestartTruncatesAgain() throws Exception {
        DecisionLog.record(build("Pylon", 100));
        assertTrue(content().contains("\"subject\":\"Pylon\""));

        // A new JVM run starts with clean statics: the next run's first
        // flush must truncate again, not append to the previous game.
        DecisionLog.clearForTests();
        DecisionLog.record(build("Gateway", 50));

        String after = content();
        assertTrue(after.contains("\"subject\":\"Gateway\""));
        assertFalse(after.contains("\"subject\":\"Pylon\""));
    }
}
