package tests.unit;

import atlantis.game.util.GameSummary;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The shutdown hook's final message must be safe when no game ever ran
 * (fresh JVM, attach failed) and must never throw - a hook that throws
 * poisons shutdown. Live proof (summary present after a wall kill) comes
 * from OpenBW runs, not from here: no stub can kill a JVM.
 */
@Tag("infra")
public class GameSummaryShutdownTest {

    @Test
    public void shutdownPrintWithNoGameIsSilentAndSafe() {
        PrintStream saved = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture));
        try {
            assertDoesNotThrow(GameSummary::printOnShutdown);
            assertDoesNotThrow(GameSummary::printOnShutdown);
        } finally {
            System.setOut(saved);
        }
        assertEquals(
            "", capture.toString(),
            "no game ran in this JVM, so there is nothing to summarise - "
                + "and the latch must make the second call a no-op too");
    }
}
