package atlantis.keyboard;

import atlantis.config.AtlantisConfig;
import atlantis.debug.painter.APainter;
import atlantis.game.AGame;
import atlantis.game.CameraCommander;
import atlantis.game.GameSpeed;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AKeyboard implements NativeKeyListener {

    public static void listenForKeyEvents() {
        turnOffLibraryLogging();

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
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
//        System.out.println("Key pressed code: " + e.getKeyCode());
        consumeEvent(e);
        
        switch (e.getKeyCode()) {
            
            // Key "Escape"
            case 1:
                System.out.println();
                System.out.println("Exit requested by the user");
                AGame.exit();
                break;

            // Key "c" / "C"
            case 46:
                CameraCommander.toggleFocusCameraOnFirstCombatUnit();
                break;

//            // Key "o" / "O"
//            case 24 : case 42:

            // Key "p" / "P"
            case 25 : case 41:
                APainter.togglePainting();
                break;

            // 3653 - PauseBreak, 57 - Space, 29 - Right Control
            case 3653: case 57: case 29:
                GameSpeed.pauseModeToggle();
                break;

            // Keys "-" and NumPad "-" and "["
            case 12: case 3658: case 26:
                GameSpeed.changeSpeedBy(+40);
//                System.out.println("Notice: SPEED SLOWER (" + GameSpeed.gameSpeed + ")");
                break;

            // Keys "+" and NumPad "+" and "]"
            case 13: case 3662: case 27:
                GameSpeed.changeSpeedBy(-40);
//                System.out.println("Notice: SPEED FASTER (" + GameSpeed.gameSpeed + ")");
                break;

            // Key "1"
            case 2:
                changeSpeedAndFrameSkip(20, 0);
                break;

            // Key "2"
            case 3:
                changeSpeedAndFrameSkip(0, 0);
                break;

            // Key "3"
            case 4:
                changeSpeedAndFrameSkip(0, 10);
                break;

            // Key "4"
            case 5:
                changeSpeedAndFrameSkip(0, 20);
                break;

            // Key "5"
            case 6:
                changeSpeedAndFrameSkip(0, 40);
                break;

            // Key "6"
            case 7:
                changeSpeedAndFrameSkip(0, 80);
                break;

            // Key "7"
            case 8:
                changeSpeedAndFrameSkip(0, 150);
                break;

            // Key "8"
            case 9:
                changeSpeedAndFrameSkip(0, 250);
                break;

            // Key "9"
            case 10:
                changeSpeedAndFrameSkip(0, 500);
                break;

            // Key "0"
            case 11:
                AGame.changeDisableUI(!AtlantisConfig.DISABLE_GUI);
                break;
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        switch (e.getKeyCode()) {
            case 3653: case 57: case 29: consumeEvent(e);
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
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void changeSpeedAndFrameSkip(int speed, int frameSkip) {
//        GameSpeed.pauseGame();
        GameSpeed.changeSpeedTo(speed);
        GameSpeed.changeFrameSkipTo(frameSkip);
//        A.sleep(20);
//        GameSpeed.unpauseGame();
    }

//    @Override
//    public void nativeKeyReleased(NativeKeyEvent e) {
//        System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
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
