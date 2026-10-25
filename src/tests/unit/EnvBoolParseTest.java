package tests.unit;

import atlantis.config.env.Env;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ENV booleans read leniently (owner 2026-10-25): "true"/"1" count as on,
 * "false"/"0" as off, anything else logs once and answers the default -
 * never a silent false.
 */
@Tag("infra")
public class EnvBoolParseTest {

    @Test
    public void trueSpellingsReadTrue() {
        assertTrue(Env.parseBool("K", "true", false));
        assertTrue(Env.parseBool("K", "TRUE", false));
        assertTrue(Env.parseBool("K", "True", false));
        assertTrue(Env.parseBool("K", "1", false));
        assertTrue(Env.parseBool("K", "  1  ", false));
    }

    @Test
    public void falseSpellingsReadFalse() {
        assertFalse(Env.parseBool("K", "false", true));
        assertFalse(Env.parseBool("K", "FALSE", true));
        assertFalse(Env.parseBool("K", "False", true));
        assertFalse(Env.parseBool("K", "0", true));
        assertFalse(Env.parseBool("K", "  0  ", true));
    }

    @Test
    public void absentReadsDefaultSilently() {
        assertTrue(Env.parseBool("K", null, true));
        assertFalse(Env.parseBool("K", null, false));
        assertTrue(Env.parseBool("K", "", true));
        assertFalse(Env.parseBool("K", "   ", false));
    }

    @Test
    public void garbageReadsDefault() {
        assertFalse(Env.parseBool("K-GARBAGE-A", "yes", false));
        assertTrue(Env.parseBool("K-GARBAGE-B", "yes", true));
        assertFalse(Env.parseBool("K-GARBAGE-C", "2", false));
        assertFalse(Env.parseBool("K-GARBAGE-D", "on", false));
    }
}
