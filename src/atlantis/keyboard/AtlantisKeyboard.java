package atlantis.keyboard;

import atlantis.AtlantisGame;
import atlantis.debug.AtlantisPainter;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class AtlantisKeyboard implements NativeKeyListener {

    public static void listenForKeyEvents() {
        turnOffLibraryLogging();

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new AtlantisKeyboard());
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
        System.out.println(e.getKeyCode());
        
        switch (e.getKeyCode()) {
            
            // Keys "-" and NumPad "-"
            case 12: case 3658:
                AtlantisGame.changeSpeedBy(+2);
                break;

            // Keys "+" and NumPad "+"
            case 13: case 3662: 
                System.out.println("Notice: SPEED FASTER!");
                AtlantisGame.changeSpeedBy(-2);
                break;

            // Key "ESCAPE"
            case 1: 
                System.out.println("ESCAPE pressed: Exit requested by user");
                atlantis.Atlantis.getInstance().onEnd(false);
                break;

            // Key "1"
            case 2: 
                changePaintingMode(AtlantisPainter.MODE_NO_PAINTING);
                break;

            // Key "2"
            case 3: 
                changePaintingMode(AtlantisPainter.MODE_PARTIAL_PAINTING);
                break;

            // Key "3"
            case 4: 
                changePaintingMode(AtlantisPainter.MODE_FULL_PAINTING);
                break;
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
//        System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
//        String keyAsString = e.getKeyText(e.getKeyCode());
    }
    
    // =========================================================

    private void changePaintingMode(int mode) {
        AtlantisPainter.paintingMode = mode;
        
        String string = "";
        if (mode == AtlantisPainter.MODE_NO_PAINTING) {
            string = "Paint mode #1: PAINTING DISABLED";
        }
        else if (mode == AtlantisPainter.MODE_PARTIAL_PAINTING) {
            string = "Paint mode #2: PAINT ONLY INFO";
        }
        else if (mode == AtlantisPainter.MODE_FULL_PAINTING) {
            string = "Paint mode #3: FULL PAINT MODE";
        }
        else {
            string = "Paint mode error";
        }
        
        AtlantisGame.sendMessage(string);
    }
    
}
