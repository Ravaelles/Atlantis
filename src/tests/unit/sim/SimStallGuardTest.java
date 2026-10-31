package tests.unit.sim;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The global sim watchdog is a pure function of (frame, bank, last warp):
 * instantiating a game to test it would be testing the harness, not the
 * rule. Every millisecond of silence past the window must fail, everything
 * before it must pass through untouched.
 */
@Tag("infra")
public class SimStallGuardTest {

    @Test
    public void healthyStartPasses() {
        assertDoesNotThrow(() -> SimStallException.check(0, 50, 0));
        assertDoesNotThrow(() -> SimStallException.check(100, 100, 90));
    }

    @Test
    public void timeCapFailsAtTwentyMinutes() {
        SimStallException thrown = assertThrows(SimStallException.class,
            () -> SimStallException.check(SimStallException.TIME_CAP_FRAMES, 0, 0));
        assertTrue(thrown.getMessage().contains("36000"),
            "the message names the cap, not just the fact: " + thrown.getMessage());
    }

    @Test
    public void bankBelow800NeverTrips() {
        assertDoesNotThrow(() -> SimStallException.check(20000, 799, 0));
    }

    @Test
    public void warpsResetTheSilenceWindow() {
        // 900 banked but the last warp was 100 frames ago: still spending.
        assertDoesNotThrow(() -> SimStallException.check(5000, 900, 4900));
    }

    @Test
    public void silentHoardTrips() {
        SimStallException thrown = assertThrows(SimStallException.class,
            () -> SimStallException.check(5000, 800, 3500));
        String message = thrown.getMessage();
        assertTrue(message.contains("5000") && message.contains("800"),
            "the message carries frame and bank for the post-mortem: " + message);
    }
}
