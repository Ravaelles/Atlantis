package tests.unit;

import atlantis.game.util.GameSummary;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-of-match block (owner 2026-11-09): the last thing the console and
 * the structured log show, in one fixed shape. Pins the exact bytes,
 * both verdicts, the time format and the twin fields - no game needed,
 * every input rides the signature.
 */
@Tag("infra")
public class MatchOverSummaryTest {

    @BeforeEach
    public void clearLog() {
        EventLog.clearForTests();
    }

    @AfterEach
    public void cleanLog() {
        EventLog.clearForTests();
    }

    @Test
    public void victoryBlockMatchesTheShape() {
        assertEquals(
            "#####################################\n"
                + "### Match over: VICTORY\n"
                + "### Killed: 84 / Lost: 16\n"
                + "### Real time: 3m0s (180s)\n"
                + "### Game time: 11m16s (frame 16224)\n"
                + "### Dir: /home/rav/.scbw/games/GAME_CFFFB780\n"
                + "#####################################",
            GameSummary.formatMatchOver(
                true, 84, 16, 180, 16224, "/home/rav/.scbw/games/GAME_CFFFB780"));
    }

    @Test
    public void defeatNamesDefeat() {
        String block = GameSummary.formatMatchOver(false, 3, 40, 95, 2400, "/tmp/game");

        assertTrue(block.contains("### Match over: DEFEAT"), block);
        assertTrue(block.contains("### Killed: 3 / Lost: 40"), block);
        assertTrue(block.contains("### Real time: 1m35s (95s)"), block);
        assertTrue(block.contains("### Game time: 1m40s (frame 2400)"), block);
    }

    @Test
    public void minSecFormats() {
        assertEquals("0m0s", GameSummary.formatMinSec(0));
        assertEquals("0m59s", GameSummary.formatMinSec(59));
        assertEquals("1m0s", GameSummary.formatMinSec(60));
        assertEquals("3m0s", GameSummary.formatMinSec(180));
        assertEquals("11m16s", GameSummary.formatMinSec(676));
    }

    @Test
    public void missingFrameStaysReadable() {
        String block = GameSummary.formatMatchOver(true, 1, 2, 10, -1, null);

        assertTrue(block.contains("### Game time: ? (frame ?)"), block);
        assertTrue(block.contains("### Dir: ?"), block);
    }

    @Test
    public void twinCarriesTheFields() {
        GameSummary.rowMatchSummary(
            true, 84, 16, 180, 16224, "/home/rav/.scbw/games/GAME_CFFFB780", 100, "0:04");

        String log = EventLog.toJsonLines();
        assertTrue(log.contains("\"key\":\"MATCH_SUMMARY\""), log);
        assertTrue(log.contains("\"result\":\"VICTORY\""), log);
        assertTrue(log.contains("\"killed\":84"), log);
        assertTrue(log.contains("\"lost\":16"), log);
        assertTrue(log.contains("\"realSeconds\":180"), log);
        assertTrue(log.contains("\"gameFrame\":16224"), log);
        assertTrue(log.contains("/home/rav/.scbw/games/GAME_CFFFB780"), log);
    }
}
