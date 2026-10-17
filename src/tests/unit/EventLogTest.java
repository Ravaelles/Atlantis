package tests.unit;

import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The structured event log pins its JSON-lines rendering exactly: one row
 * per event, key first, parameters in call order, numbers unquoted.
 */
public class EventLogTest {

    // Both ends: refusal paths anywhere in the suite record rows into the
    // shared buffer (that is the twin working), so this class starts clean
    // no matter which test ran before it in the JVM.
    @BeforeEach
    public void clearBefore() {
        EventLog.clearForTests();
    }

    @AfterEach
    public void clearLog() {
        EventLog.clearForTests();
    }

    @Test
    public void twoEventsRenderAsJsonLines() {
        EventLog.key(LogKey.WORKER_GATE)
            .param("reason", "CantAfford")
            .param("workers", 5)
            .param("minerals", 32)
            .log(30, "0:01");
        EventLog.key(LogKey.BOARD)
            .param("top", "workers")
            .param("idle", false)
            .log(60, "0:02");

        assertEquals(
            "{\"frame\":30,\"time\":\"0:01\",\"key\":\"WORKER_GATE\","
                + "\"reason\":\"CantAfford\",\"workers\":5,\"minerals\":32}\n"
                + "{\"frame\":60,\"time\":\"0:02\",\"key\":\"BOARD\","
                + "\"top\":\"workers\",\"idle\":false}\n",
            EventLog.toJsonLines()
        );
    }

    @Test
    public void flushDeliversBufferAndEmptiesIt() throws Exception {
        File tmp = File.createTempFile("events", ".jsonl");
        tmp.deleteOnExit();

        EventLog.key(LogKey.WORKER_GATE).param("reason", "NoSupply").log(30, "0:01");
        EventLog.flushTo(tmp.getAbsolutePath());

        String content = new String(java.nio.file.Files.readAllBytes(tmp.toPath()), "UTF-8");
        assertEquals(
            "{\"frame\":30,\"time\":\"0:01\",\"key\":\"WORKER_GATE\",\"reason\":\"NoSupply\"}\n",
            content
        );
        assertEquals("", EventLog.toJsonLines());
    }

    @Test
    public void flushDueFiresOnInterval() {
        assertFalse(EventLog.flushDue(719, 0));
        assertTrue(EventLog.flushDue(720, 0));
        assertTrue(EventLog.flushDue(1500, 720));
    }

    @Test
    public void firstFlushTruncatesStaleFileSecondAppends() throws Exception {
        File tmp = File.createTempFile("events-stale", ".jsonl");
        tmp.deleteOnExit();
        java.nio.file.Files.write(
            tmp.toPath(), "STALE ROW FROM A PREVIOUS GAME\n".getBytes("UTF-8"));

        EventLog.key(LogKey.WORKER_GATE).param("reason", "NoSupply").log(30, "0:01");
        EventLog.flushTo(tmp.getAbsolutePath());

        String afterFirst = new String(java.nio.file.Files.readAllBytes(tmp.toPath()), "UTF-8");
        assertEquals(
            "{\"frame\":30,\"time\":\"0:01\",\"key\":\"WORKER_GATE\",\"reason\":\"NoSupply\"}\n",
            afterFirst);

        EventLog.key(LogKey.BOARD).param("top", "workers").log(60, "0:02");
        EventLog.flushTo(tmp.getAbsolutePath());

        String afterSecond = new String(java.nio.file.Files.readAllBytes(tmp.toPath()), "UTF-8");
        assertTrue(afterSecond.startsWith(
            "{\"frame\":30,\"time\":\"0:01\",\"key\":\"WORKER_GATE\",\"reason\":\"NoSupply\"}\n"));
        assertTrue(afterSecond.contains("\"key\":\"BOARD\""));
        assertFalse(afterSecond.contains("STALE ROW"));
    }

    @Test
    public void frameGoingBackwardsStartsNewGame() {
        EventLog.key(LogKey.WORKER_GATE).param("reason", "NoSupply").log(600, "0:25");
        EventLog.key(LogKey.WORKER_GATE).param("reason", "CantAfford").log(5, "0:00");

        assertEquals(
            "{\"frame\":5,\"time\":\"0:00\",\"key\":\"WORKER_GATE\",\"reason\":\"CantAfford\"}\n",
            EventLog.toJsonLines()
        );
    }
}
