package tests.acceptance;

import atlantis.units.AUnitType;
import atlantis.util.log.Log;
import org.junit.Test;
import tests.unit.FakeUnit;

import static org.junit.Assert.*;

public class LogTest extends NonAbstractTestFakingGame {
    @Test
    public void testAddMessage() {
        FakeUnit unit = fakeUnit();
        Log log = unit.log();

        assertEquals(0, log.messages().size());
        assertFalse(log.lastMessageWas("A1"));
        assertEquals(null, log.lastMessage());

        unit.addLog("A1");

        assertEquals(1, log.messages().size());
        assertTrue(log.lastMessageWas("A1"));
        assertEquals("A1", log.lastMessage().message());
        assertTrue(log.toString().contains("0: A1"));

        unit.addLog("A2");

        assertEquals(2, log.messages().size());
        assertTrue(log.lastMessageWas("A2"));
        assertEquals("A2", log.lastMessage().message());
        assertTrue(log.toString().contains("0: A1"));
        assertTrue(log.toString().contains("0: A2"));

        log.replaceLastWith("B2", unit);

        assertEquals(2, log.messages().size());
        assertTrue(log.lastMessageWas("B2"));
        assertEquals("B2", log.lastMessage().message());
        assertTrue(log.toString().contains("0: A1"));
        assertFalse(log.toString().contains("0: A2"));
        assertTrue(log.toString().contains("0: B2"));
    }

    private FakeUnit fakeUnit() {
        return fake(AUnitType.Protoss_Dragoon, 10);
    }
}
