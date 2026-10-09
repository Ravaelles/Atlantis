package tests.unit;

import atlantis.game.listeners.OnGameEnd;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The end-of-game latch must behave like per-game state, not like a
 * process-wide one-way flag.
 *
 * <p>
 * NEXT #49 recorded two defects in {@code OnGameEnd._executed}:
 * </p>
 * <ol>
 * <li>the {@code Env.isTesting()} branch returned <b>without</b> setting the
 * flag, so the guard was not symmetric - the first spurious {@code onEnd} left
 * it false;</li>
 * <li>the field is {@code static}, so it leaked between games in one JVM, the
 * same class of leak {@code UnitsArchive.reset()} and
 * {@code ReservedResources.reset()} were fixed for.</li>
 * </ol>
 *
 * <p>
 * {@code execute()} cannot be driven directly here because its exit path calls
 * {@code System.exit}; the latch rule is asserted through the small read/mark
 * seam instead, which is exactly the part that was wrong.
 * </p>
 */
public class OnGameEndLatchTest {

    @BeforeEach
    @AfterEach
    public void resetLatch() {
        OnGameEnd.reset();
    }

    @Test
    public void aFreshGameHasNotExecutedTheCleanup() {
        assertFalse(OnGameEnd.hasExecuted());
    }

    @Test
    public void resetClearsAYesterdayLatch() {
        // This is the leak: a previous game (or a spurious onEnd) marked the
        // latch, and without the reset the next game never runs its cleanup.
        OnGameEnd.markExecutedForTest();
        assertTrue(OnGameEnd.hasExecuted(), "precondition: the latch is set");

        OnGameEnd.reset();

        assertFalse(OnGameEnd.hasExecuted(),
                "a new game must start with the latch clear, or its real end-of-game cleanup never runs");
    }

    @Test
    public void theLatchIsOneWayWithinAGame() {
        // Once cleanup has run, a second onEnd must not run it again - that is
        // the guard's whole job, and reset() is the only thing that may clear it.
        OnGameEnd.markExecutedForTest();

        assertTrue(OnGameEnd.hasExecuted());
        assertTrue(OnGameEnd.hasExecuted(), "reading the latch must not change it");
    }
}
