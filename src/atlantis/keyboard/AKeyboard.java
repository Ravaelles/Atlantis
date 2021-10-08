package atlantis.keyboard;

import atlantis.AGame;
import atlantis.AGameSpeed;
import atlantis.AtlantisConfig;
import atlantis.debug.APainter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

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
        
        switch (e.getKeyCode()) {
            
            // Key "Escape" and "`"
            case 1: case 41:
                System.out.println();
                System.out.println("Exit requested by the user");
                AGame.exit();
                break;

            // Key "PauseBreak"
            case 3653:
                AGame.pauseModeToggle();
//                System.out.println("Notice: " + (AGame.isPaused() ? "" : "UN") + "PAUSED");
                break;

            // Keys "-" and NumPad "-" and "["
            case 12: case 3658: case 26:
                AGameSpeed.changeSpeedBy(+20);
                System.out.println("Notice: SPEED SLOWER (" + AGameSpeed.gameSpeed + ")");
                break;

            // Keys "+" and NumPad "+" and "]"
            case 13: case 3662: case 27:
                AGameSpeed.changeSpeedBy(-20);
                System.out.println("Notice: SPEED FASTER (" + AGameSpeed.gameSpeed + ")");
                break;

            // Key "1"
            case 2: 
                AGameSpeed.changeFrameSkipTo(1);
                break;

            // Key "2"
            case 3:
                AGameSpeed.changeFrameSkipTo(2);
                break;

            // Key "3"
            case 4:
                AGameSpeed.changeFrameSkipTo(3);
                break;

            // Key "4"
            case 5:
                AGameSpeed.changeFrameSkipTo(4);
                break;

            // Key "5"
            case 6:
                AGameSpeed.changeFrameSkipTo(5);
                break;

            // Key "6"
            case 7:
                AGameSpeed.changeFrameSkipTo(6);
                break;

            // Key "7"
            case 8:
                AGameSpeed.changeFrameSkipTo(7);
                break;

            // Key "8"
            case 9:
                AGameSpeed.changeFrameSkipTo(8);
                break;

            // Key "9"
            case 10:
                AGameSpeed.changeFrameSkipTo(9);
                break;

            // Key "0"
            case 11:
                AGame.changeDisableUI(!AtlantisConfig.DISABLE_GUI);
                break;
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
//        System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

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
