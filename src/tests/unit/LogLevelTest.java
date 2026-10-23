package tests.unit;

import atlantis.util.log.LogLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Console and twin tags (CONVENTIONS §27): short, greppable, ordered.
 * LOW < MEDIUM < HIGH < CRITICAL; every rendered line carries one.
 */
@Tag("infra")
public class LogLevelTest {

    @Test
    public void tagsAreShortBracketedCodes() {
        assertEquals("[LOW]", LogLevel.LOW.tag());
        assertEquals("[MED]", LogLevel.MEDIUM.tag());
        assertEquals("[HIGH]", LogLevel.HIGH.tag());
        assertEquals("[CRIT]", LogLevel.CRITICAL.tag());
    }

    @Test
    public void codesAreShortJsonValues() {
        assertEquals("LOW", LogLevel.LOW.code());
        assertEquals("MED", LogLevel.MEDIUM.code());
        assertEquals("HIGH", LogLevel.HIGH.code());
        assertEquals("CRIT", LogLevel.CRITICAL.code());
    }

    @Test
    public void orderingIsLowMediumHighCritical() {
        assertTrue(LogLevel.CRITICAL.atLeast(LogLevel.HIGH));
        assertTrue(LogLevel.HIGH.atLeast(LogLevel.MEDIUM));
        assertTrue(LogLevel.MEDIUM.atLeast(LogLevel.LOW));
        assertTrue(LogLevel.LOW.atLeast(LogLevel.LOW));
    }
}
