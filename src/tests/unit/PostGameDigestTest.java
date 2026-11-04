package tests.unit;

import atlantis.util.AConsole;
import atlantis.util.log.LogKey;
import atlantis.util.log.LogLevel;
import atlantis.util.log.LogPort;
import atlantis.util.log.PostGameDigest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The end-of-game digest pins its grouping contract: per [LEVEL] KEY
 * counts with at most ten witnesses per key, console lines counted by
 * level with HIGH/CRIT samples only, and a compact JSON file the agent
 * reads instead of paging the log (owner order 2026-11-04). Engine-free
 * by construction: feeds are plain key/level/text calls.
 */
@Tag("infra")
public class PostGameDigestTest {

    private static LogPort originalPort;

    private final RecordingLogPort recording = new RecordingLogPort();

    @BeforeEach
    public void setUp() {
        originalPort = AConsole.port();
        AConsole.usePort(recording);
        AConsole.clearCappedForTests();
        PostGameDigest.clearForTests();
    }

    @AfterEach
    public void tearDown() {
        AConsole.usePort(originalPort);
        AConsole.clearCappedForTests();
        PostGameDigest.clearForTests();
    }

    @Test
    public void twinRowsGroupByLevelAndKey() {
        PostGameDigest.note(LogKey.STALL, LogLevel.HIGH, "STALL f=1");
        PostGameDigest.note(LogKey.STALL, LogLevel.HIGH, "STALL f=2");
        PostGameDigest.note(LogKey.POSITION_REFRESH, LogLevel.MEDIUM, "refresh");

        assertEquals(2, PostGameDigest.countFor("[HIGH] STALL"));
        assertEquals(1, PostGameDigest.countFor("[MED] POSITION_REFRESH"));
        assertEquals(0, PostGameDigest.countFor("[HIGH] STALL ") + PostGameDigest.countFor("STALL"));
    }

    @Test
    public void samplesCapAtTenPerKeyButCountsDoNot() {
        for (int i = 0; i < 12; i++) {
            PostGameDigest.note(LogKey.STALL, LogLevel.HIGH, "STALL f=" + i);
        }

        assertEquals(12, PostGameDigest.countFor("[HIGH] STALL"));
        assertEquals(10, PostGameDigest.samplesFor("[HIGH] STALL").size());
        assertTrue(PostGameDigest.samplesFor("[HIGH] STALL").get(0).contains("f=0"));
    }

    @Test
    public void consoleLinesCountByLevelWithHighSamplesOnly() {
        AConsole.println(LogLevel.HIGH, "boom");
        AConsole.println("chatter");

        assertEquals(1, PostGameDigest.countFor("[HIGH] CONSOLE"));
        assertEquals(1, PostGameDigest.countFor("[MED] CONSOLE"));
        assertEquals(1, PostGameDigest.samplesFor("[HIGH] CONSOLE").size());
        assertTrue(PostGameDigest.samplesFor("[HIGH] CONSOLE").get(0).contains("boom"));
        assertTrue(PostGameDigest.samplesFor("[MED] CONSOLE").isEmpty());
    }

    @Test
    public void digestOwnSectionDoesNotFeedBackIntoCounts() {
        PostGameDigest.note(LogKey.STALL, LogLevel.HIGH, "STALL f=1");
        PostGameDigest.printSummary(4, 0, 0);

        assertEquals(1, PostGameDigest.countFor("[HIGH] STALL"));
        assertEquals(0, PostGameDigest.countFor("[MED] CONSOLE"));
        assertTrue(recording.out.stream().anyMatch(line ->
            line.contains("POSTGAME DIGEST") && line.contains("warps=4")));
    }

    @Test
    public void jsonCarriesErrorsByKeySamplesAndConstruction() throws Exception {
        PostGameDigest.note(LogKey.STALL, LogLevel.HIGH, "STALL f=1");
        PostGameDigest.note(LogKey.SUPPLY_OVERSTOCK, LogLevel.CRITICAL, "overstock");

        java.io.File tmp = Files.createTempFile("digest", ".json").toFile();
        PostGameDigest.writeTo(tmp.getAbsolutePath(), "{\"warps\":3,\"offPlan\":1,\"sameTileDups\":0}");

        String json = new String(Files.readAllBytes(tmp.toPath()), "UTF-8");
        assertTrue(json.contains("\"HIGH\":1"), json);
        assertTrue(json.contains("\"CRIT\":1"), json);
        assertTrue(json.contains("\"[HIGH] STALL\":1"), json);
        assertTrue(json.contains("\"warps\":3"), json);
        assertTrue(json.contains("\"offPlan\":1"), json);
    }

    private static class RecordingLogPort implements LogPort {

        private final List<String> out = new ArrayList<>();

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
            out.add(String.valueOf(message));
        }

        @Override
        public void printStackTrace(String message) {
        }
    }
}
