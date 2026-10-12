package tests.unit;

import atlantis.Atlantis;
import atlantis.config.MapSpecificCommander;
import atlantis.game.A;
import atlantis.game.GameSpeed;
import atlantis.keyboard.AKeyboard;
import bwapi.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A speed change must never leave the bot paused.
 *
 * <p>Both paths pause first and unpause last; if the speed call itself throws
 * between the two, the unpause is skipped and {@code OnEveryFrame} spins in
 * its pause loop forever - a game that looks paused from the start until a
 * human presses "2" (owner report, IDE/Wine run).</p>
 */
public class KeyboardPauseReleaseTest {

    @AfterEach
    public void releaseStaticPause() {
        GameSpeed.unpauseGame();
    }

    @Test
    public void speedChangeReleasesPauseEvenWhenSpeedCallThrows() {
        boolean initialSpeedFlag = MapSpecificCommander.initialSpeed;
        GameSpeed.unpauseGame();
        A.setNow(0, 0);

        Game mockGame = Mockito.mock(Game.class);
        Mockito.doThrow(new RuntimeException("boom"))
            .when(mockGame).setLocalSpeed(Mockito.anyInt());

        try (MockedStatic<Atlantis> atlantis = Mockito.mockStatic(Atlantis.class)) {
            atlantis.when(Atlantis::game).thenReturn(mockGame);

            // The speed failure itself still propagates to the caller - the
            // guarantee is only that the pause does not survive it.
            try {
                AKeyboard.changeSpeedAndFrameSkip(0, 0);
            } catch (RuntimeException expected) {
            }

            assertFalse(
                GameSpeed.isPaused(),
                "pause must lift even when the speed change itself throws"
            );
        }
        finally {
            MapSpecificCommander.initialSpeed = initialSpeedFlag;
            GameSpeed.unpauseGame();
        }
    }
}
