package atlantis.keyboard;

import atlantis.config.AtlantisConfig;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.CameraCommander;
import atlantis.game.GameSpeed;
import atlantis.keyboard.actions.Exit;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AKeyboard implements NativeKeyListener {
    /**
     * True when this JVM is the bot itself - the one running under Wine with
     * the game attached (it has BWAPI, so speed/camera shortcuts can work).
     * The Linux supervisor JVM starts with no game and stays without one.
     */
    private static boolean isBotJvm() {
        if (System.getenv("WINEDEBUG") != null || System.getenv("WINEDLLOVERRIDES") != null) return true;
        return atlantis.Atlantis.game() != null;
    }

    /**
     * It looks that Starcraft needs to receive a pause with a delay, otherwise the speed settings don't work.
     */
    public static final int MS_DELAY_FIX = 25;

    public static void listenForKeyEvents() {
        turnOffLibraryLogging();

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            // Not fatal: on the Windows/Wine side this means the hook failed
            // inside Wine (no desktop access etc.) - the game still runs, only
            // the shortcuts are dead. Say so and keep going instead of dying.
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            return;
        }

        GlobalScreen.addNativeKeyListener(new AKeyboard());
    }

    private static void turnOffLibraryLogging() {

        // Get the logger for "org.jnativehook" and set the level to off.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        // Don't forget to disable the parent handlers.
        logger.setUseParentHandlers(false);
    }

    // =========================================================

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        consumeEvent(e);
        dispatchKeyCode(e.getKeyCode(), e.getKeyLocation());
    }

    /**
     * Executes one key action. Used both directly from the keyboard hook and
     * from the KeyRelay thread - the Wine setup has two JVMs and only the
     * Linux supervisor's hook sees global keystrokes, so the supervisor
     * forwards key codes to the bot, which owns the game.
     *
     * <p>{@code keyLocation} distinguishes left from right for keys that have
     * both (Ctrl, Shift, Alt) - the hook's {@code NativeKeyEvent.getKeyLocation()}:
     * 2 = left, 3 = right, 1 = standard, 0 = unknown. Keys with no location
     * distinction (letters, digits) pass 1 and it is ignored.</p>
     */
    public static void dispatchKeyCode(int keyCode) {
        dispatchKeyCode(keyCode, 1);
    }

    public static void dispatchKeyCode(int keyCode, int keyLocation) {        // The supervisor JVM (no game attached) cannot execute game actions -
        // but its hook is the one that sees global keystrokes (the bot's hook
        // under Wine only sees keys typed into Wine windows). Forward everything
        // to the bot through the relay file; Escape stays here, because killing
        // the game is exactly the supervisor's job.
        if (!isBotJvm()) {
            if (keyCode == 1) Exit.handle();       // Escape
            else KeyRelay.send(keyCode, keyLocation);
            return;
        }

        switch (keyCode) {

            // ######### EXIT GAME #########
            case 1:
                // Key "Escape"
                Exit.handle();
                break;

            // ######### SPEED - SLOWER #########
            case 12:
            case 3658:
            case 26:
                // Keys "-" and NumPad "-" and "["
                gameSpeedSlower();
                break;

            // ######### SPEED - FASTER #########
            case 13:
            case 3662:
            case 27:
                // Keys "+" and NumPad "+" and "]"
                gameSpeedFaster();
                break;

            // ######### CAMERA FOCUS ON COMBAT UNIT #########
            case 46:
                // Key "c" / "C"
                CameraCommander.toggleFocusCameraOnInterestingCombatUnit();
                break;

//            // Key "o" / "O"
//            case 24 : case 42:

            // ######### TOGGLE PAINTING OF EXTRA INFO #########
            case 25:
                // Key "p" / "P" (VC_P = 25 per NativeKeyEvent)
                APainter.togglePainting();
                break;

            // ######### UN/PAUSE GAME #########
            // 3653 - PauseBreak, 57 - Space, 41 - tilde/backquote,
            // 29 - LEFT Ctrl, 3665 - RIGHT Ctrl (measured 2026-10-06 from the
            // owner's live hook output: right Ctrl reports its own code, not
            // 29 with a right location - which is why every location-based
            // gate killed it). Both Ctrl keys pause, owner's call.
            case 3653:
            case 57:
            case 41:
            case 3665:
                GameSpeed.pauseModeToggle();
                break;

            // ######### GAME SPEED 1 (natural) #########
            case 2:
                // Key "1"
                changeSpeedAndFrameSkip(20, 0);
                break;

            // ######### GAME SPEED 2 (fast) #########
            case 3:
                // Key "2"
                changeSpeedAndFrameSkip(0, 0);
                break;

            // ######### GAME SPEED 3 (very fast) #########
            case 4:
                // Key "3"
                changeSpeedAndFrameSkip(0, 10);
                break;

            // ######### GAME SPEED 4 (extra super fast) #########
            case 5:
                // Key "4"
                changeSpeedAndFrameSkip(0, 20);
                break;

            // ######### GAME SPEED 5 (fucking super fast) #########
            case 6:
                // Key "5"
                changeSpeedAndFrameSkip(0, 40);
                break;

            // ######### GAME SPEED 6 (oh my fucking how fast) #########
            case 7:
                // Key "6"
                changeSpeedAndFrameSkip(0, 80);
                break;

            // ######### GAME SPEED 7 (ridi-fuckin-culously fast) #########
            case 8:
                // Key "7"
                changeSpeedAndFrameSkip(0, 150);
                break;

            // ######### GAME SPEED 8 (lightning-shit-quick) #########
            case 9:
                // Key "8"
                changeSpeedAndFrameSkip(0, 250);
                break;

            // ######### GAME SPEED 9 (don't-make-me-come-up-with-name-how-fast)
            case 10:
                // Key "9"
                changeSpeedAndFrameSkip(0, 500);
                break;

            // ######### DISABLE GUI - runs considerably faster #########
            case 11:
                // Key "0"
                AGame.changeDisableUI(!AtlantisConfig.DISABLE_GUI);
                break;
        }
    }

    private static void gameSpeedFaster() {
        changeGameSpeed(1);
    }

    private static void gameSpeedSlower() {
        changeGameSpeed(-1);
    }

    private static void changeGameSpeed(int positiveForQuickerNegativeForSlower) {
        GameSpeed.pauseGame();
        A.sleep(MS_DELAY_FIX); // Needed otherwise for some reason it doesn't work

        int deltaChangeSpeed = 15;
        GameSpeed.changeSpeed(deltaChangeSpeed * (positiveForQuickerNegativeForSlower > 0 ? -1 : +1));

        A.sleep(MS_DELAY_FIX); // Needed otherwise for some reason it doesn't work
        GameSpeed.unpauseGame();
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        switch (e.getKeyCode()) {
            case 3653:
            case 57:
            case 29:
                consumeEvent(e);
        }
    }

    /**
     * Doesn't work on some OS e.g. on Linux.
     * See: https://github.com/kwhat/jnativehook/blob/2.2/doc/ConsumingEvents.md
     */
    private void consumeEvent(NativeKeyEvent e) {
        try {
            Field f = NativeInputEvent.class.getDeclaredField("reserved");
            f.setAccessible(true);
            f.setShort(e, (short) 0x01);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void changeSpeedAndFrameSkip(int speed, int frameSkip) {
        GameSpeed.pauseGame();

        A.sleep(MS_DELAY_FIX); // Needed otherwise for some reason it doesn't work

        GameSpeed.changeSpeedTo(speed);
        GameSpeed.changeFrameSkipTo(frameSkip);

        A.sleep(MS_DELAY_FIX); // Needed otherwise for some reason it doesn't work

        GameSpeed.unpauseGame();
    }

//    @Override
//    public void nativeKeyReleased(NativeKeyEvent e) {

//    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
//        String keyAsString = e.getKeyText(e.getKeyCode());
    }

    // =========================================================

    private void changePaintingMode(int mode) {
        APainter.paintingMode = mode;

        String string;
        switch (mode) {
            case APainter.MODE_NO_PAINTING:
                string = "Paint mode #1: PAINTING DISABLED";
                break;
            case APainter.MODE_PARTIAL_PAINTING:
                string = "Paint mode #2: PAINT ONLY INFO";
                break;
            case APainter.MODE_FULL_PAINTING:
                string = "Paint mode #3: FULL PAINT MODE";
                break;
            default:
                string = "Paint mode error";
                break;
        }

        AGame.sendMessage(string);
    }

}
