package tests.acceptance;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.util.log.Log;
import atlantis.util.log.LogMessage;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class LogTest extends WorldStubForTests {
    @Test
    public void testAddMessage() {
        FakeUnit unit = fakeUnit();
        Log log = unit.log();

        assertEquals(0, messages(log).size());
        assertFalse(log.lastMessageWas("A1"));
        assertEquals(null, log.lastMessage());

        unit.addLog("A1");

        assertEquals(1, messages(log).size());
        assertTrue(log.lastMessageWas("A1"));
        assertEquals("A1", log.lastMessage().message());
        assertTrue(log.toString().contains("0: A1"));

        unit.addLog("A2");

        assertEquals(2, messages(log).size());
        assertTrue(log.lastMessageWas("A2"));
        assertEquals("A2", log.lastMessage().message());
        assertTrue(log.toString().contains("0: A1"));
        assertTrue(log.toString().contains("0: A2"));

        log.replaceLastWith("B2", A.now());

        assertEquals(2, messages(log).size());
        assertTrue(log.lastMessageWas("B2"));
        assertEquals("B2", log.lastMessage().message());
        assertTrue(log.toString().contains("0: A1"));
        assertFalse(log.toString().contains("0: A2"));
        assertTrue(log.toString().contains("0: B2"));
    }

    /**
     * {@code Log} takes "now" from its caller rather than reading the clock itself
     * (see {@code LogMessage}), so the test reads the same harness clock instead
     * of asserting against a second source of time.
     */
    private java.util.List<LogMessage> messages(Log log) {
        return log.messages(A.now(), A.realSecondsNow());
    }

    private FakeUnit fakeUnit() {
        return fake(AUnitType.Protoss_Dragoon, 10);
    }
}
