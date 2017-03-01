package atlantis.keyboard;

import atlantis.AGame;
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
//        System.out.println(e.getKeyCode());
        
        switch (e.getKeyCode()) {
            
            // Key "PauseBreak"
            case 3653:
                AGame.pauseModeToggle();
                System.out.println("Notice: " + (AGame.isPaused() ? "" : "UN") + "PAUSED");
                break;
            
            // Keys "-" and NumPad "-"
            case 12: case 3658:
                AGame.changeSpeedBy(+20);
                System.out.println("Notice: SPEED SLOWER (" + AtlantisConfig.GAME_SPEED + ")");
                break;

            // Keys "+" and NumPad "+"
            case 13: case 3662: 
                AGame.changeSpeedBy(-20);
                System.out.println("Notice: SPEED FASTER (" + AtlantisConfig.GAME_SPEED + ")");
                break;

            // Key "ESCAPE"
            case 1: 
                System.out.println();
                System.out.println("ESC pressed: exit requested by the user");
                AGame.exit();
                break;

            // Key "1"
            case 2: 
                changePaintingMode(APainter.MODE_NO_PAINTING);
                break;

            // Key "2"
            case 3: 
                changePaintingMode(APainter.MODE_PARTIAL_PAINTING);
                break;

            // Key "3"
            case 4: 
                changePaintingMode(APainter.MODE_FULL_PAINTING);
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
