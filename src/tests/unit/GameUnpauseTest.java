package tests.unit;

import atlantis.Atlantis;
import bwapi.Game;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pins the forced unpause (owner report, twice, 2026-10-07: "the game starts
 * paused, you have to press control").
 *
 * <p>
 * A game that sits paused looks exactly like a bot that attached and does
 * nothing: HELLO_ATLANTIS prints, frames do not advance, no error anywhere. BWAPI
 * only exposes <em>pause</em> plus {@code resumeGame()} - there is no
 * {@code setPaused(false)} - so the bot must send the resume itself, and it
 * must do so on start and through the first frames (Wine re-pauses when the
 * window loses focus while the desktop is being arranged).
 * </p>
 *
 * <p>
 * The method is private and the interesting behaviour is one side effect (a
 * {@code resumeGame()} call exactly when paused, and never when it is not), so
 * this test drives it directly by reflection against a mocked {@link Game}.
 * That is narrower than standing up the whole commander and it asserts the
 * actual contract rather than a proxy for it.
 * </p>
 */
public class GameUnpauseTest {

    @Test
    public void pausedGameIsResumed() throws Exception {
        Game game = Mockito.mock(Game.class);
        when(game.isPaused()).thenReturn(true);

        invokeUnpauseIfPaused(game);

        verify(game, times(1)).resumeGame();
    }

    @Test
    public void runningGameIsNotDisturbed() throws Exception {
        Game game = Mockito.mock(Game.class);
        when(game.isPaused()).thenReturn(false);

        invokeUnpauseIfPaused(game);

        verify(game, never()).resumeGame();
    }

    @Test
    public void aFailingResumeDoesNotEscape() throws Exception {
        // On the first frames the game may not be interactive yet and
        // resumeGame() can throw. The bot must survive that: a failed unpause
        // is not a reason to lose the game.
        Game game = Mockito.mock(Game.class);
        when(game.isPaused()).thenReturn(true);
        Mockito.doThrow(new IllegalStateException("not interactive yet")).when(game).resumeGame();

        invokeUnpauseIfPaused(game);

        verify(game, times(1)).resumeGame();
    }

    @Test
    public void noGameObjectIsHandled() throws Exception {
        // Nothing to resume before the game exists; and crucially, no NPE.
        invokeUnpauseIfPaused(null);
    }

    /**
     * Calls the private {@code Atlantis.unpauseIfPaused(String)} with the game
     * installed on the singleton, which is how the real call path sees it.
     */
    private static void invokeUnpauseIfPaused(Game game) throws Exception {
        Atlantis atlantis = Atlantis.getInstance();
        atlantis.setGame(game);

        Method method = Atlantis.class.getDeclaredMethod("unpauseIfPaused", String.class);
        method.setAccessible(true);

        try {
            method.invoke(atlantis, "test");
        } finally {
            atlantis.setGame(null);
        }
    }

    @Test
    public void theResumeCallIsTheOnlyOneBwapiOffers() throws Exception {
        // Guard against a future "fix" that reaches for setPaused(false):
        // measured against the vendored jar, BWAPI has pauseGame/isPaused and
        // resumeGame - there is no setPaused. If this ever fails, the jar
        // changed and the unpause path must be re-read, not re-guessed.
        boolean hasResume = false;
        boolean hasSetPaused = false;
        for (Method m : Game.class.getMethods()) {
            if (m.getName().equals("resumeGame"))
                hasResume = true;
            if (m.getName().equals("setPaused"))
                hasSetPaused = true;
        }

        assertEquals(true, hasResume, "Game.resumeGame() must exist - the unpause depends on it");
        assertEquals(false, hasSetPaused,
                "Game.setPaused appears in the jar; re-read how pause is lifted before changing this path");
    }
}
