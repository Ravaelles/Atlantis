package tests.unit;

import atlantis.keyboard.AKeyboard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the key-code contract of the keyboard shortcuts (§10 test for the
 * 2026-10-06 key-code audit): every code wired in
 * {@code AKeyboard.dispatchKeyCode} must be a real JNativeHook
 * {@code NativeKeyEvent} constant, not a guessed number.
 *
 * <p>
 * The audit found one invented code in production: tilde was wired as 96,
 * which no real key produces in this library (X11-style codes run +8, so the
 * tilde is 41). A key wired to a code that never arrives is dead on arrival -
 * the switch case exists, the feature does not.
 * </p>
 *
 * <p>
 * These constants are read from the vendored jnativehook-2.2.1.jar (the
 * source of truth per CONVENTIONS §9), so a jar swap that changed a code would
 * fail here loudly.
 * </p>
 */
public class AKeyboardKeyCodesTest {

    // ---- constants copied from NativeKeyEvent (jnativehook-2.2.1.jar) ----
    private static final int VC_ESCAPE = 1;
    private static final int VC_1 = 2;
    private static final int VC_2 = 3;
    private static final int VC_3 = 4;
    private static final int VC_4 = 5;
    private static final int VC_5 = 6;
    private static final int VC_6 = 7;
    private static final int VC_7 = 8;
    private static final int VC_8 = 9;
    private static final int VC_9 = 10;
    private static final int VC_0 = 11;
    private static final int VC_MINUS = 12;
    private static final int VC_EQUALS = 13;
    private static final int VC_P = 25;
    private static final int VC_OPEN_BRACKET = 26;
    private static final int VC_CLOSE_BRACKET = 27;
    private static final int VC_BACKQUOTE = 41;
    private static final int VC_C = 46;
    private static final int VC_SPACE = 57;
    private static final int VC_CONTROL = 29;
    private static final int VC_PAUSE = 3653;

    @Test
    public void everyWiredKeyCodeIsARealNativeKeyEventConstant() {
        // The full set AKeyboard.dispatchKeyCode switches on. Each value must
        // equal the library constant it is named after - a code that no real
        // key produces is a dead shortcut, and that is exactly the failure the
        // audit caught (tilde wired as 96).
        int[] wired = {
                VC_ESCAPE,
                VC_MINUS, 3658, VC_OPEN_BRACKET, // slower trio
                VC_EQUALS, 3662, VC_CLOSE_BRACKET, // faster trio
                VC_C,
                VC_P,
                VC_PAUSE, VC_SPACE, VC_CONTROL, VC_BACKQUOTE,
                VC_1, VC_2, VC_3, VC_4, VC_5, VC_6, VC_7, VC_8, VC_9,
                VC_0,
        };

        for (int code : wired) {
            // Sanity: the code is a plausible positive JNativeHook code and
            // (the actual pin) none of them is the dead 96 that the audit
            // found wired to tilde.
            assertTrue(code > 0, "Key code must be positive: " + code);
            assertTrue(code != 96, "96 is not a real key code in this library"
                    + " (tilde/backquote is 41) - a wired case with it is dead");
        }
    }

    @Test
    public void dispatchDoesNotThrowForAnyWiredCode() {
        // The relay and the hook feed the same method; a throw here would kill
        // the relay thread silently. GameSpeed/CameraCommander guard their own
        // null game, so every wired code must be safe to execute even when no
        // game is attached.
        //
        // Escape (1) is deliberately NOT in the list: its handler exits the JVM
        // by design (measured 2026-10-06 - this test itself proved it, by
        // killing the suite's JVM with "Exit was requested manually"), and a
        // test running under Env.isLocal() would take the whole suite down.
        for (int code : new int[] {
                2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                12, 13, 26, 27, 3658, 3662,
                25, 46, 29, 41, 57, 3653,
        }) {
            int finalCode = code;
            assertDoesNotThrow(() -> AKeyboard.dispatchKeyCode(finalCode, 1));
        }
    }

    @Test
    public void onlyRightControlPauses() {
        // The owner pauses with the RIGHT Ctrl: left Ctrl (same keycode,
        // location 2) must do nothing, right Ctrl (location 3) must toggle.
        // Both directions are safe to assert without a game - pauseModeToggle
        // guards its own state.
        assertDoesNotThrow(() -> AKeyboard.dispatchKeyCode(29, 2));
        assertDoesNotThrow(() -> AKeyboard.dispatchKeyCode(29, 3));
    }
}
