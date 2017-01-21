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
    public void nativeKeyPressed(NativeKeyEvent e) {
//        System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
//
//        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
//            GlobalScreen.unregisterNativeHook();
//        }

        System.out.println("Key pressed: " + e.getKeyCode());

        switch (e.getKeyCode()) {
            
            // Keys "-" and NumPad "-"
            case 12: case 3658: 
                AtlantisGame.changeSpeedBy(+2);
                break;

            // Keys "+" and NumPad "+"
            case 13: case 3662: 
                AtlantisGame.changeSpeedBy(-2);
                break;

            // Key "1"
            case 2: 
                AtlantisPainter.paintingMode = AtlantisPainter.MODE_NO_PAINTING;
                break;

            // Key "2"
            case 3: 
                AtlantisPainter.paintingMode = AtlantisPainter.MODE_PARTIAL_PAINTING;
                break;

            // Key "3"
            case 4: 
                AtlantisPainter.paintingMode = AtlantisPainter.MODE_FULL_PAINTING;
                break;
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
//        System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
//        String keyAsString = e.getKeyText(e.getKeyCode());
    }

//        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
//                new KeyEventDispatcher() {
//
//            @Override
//            public boolean dispatchKeyEvent(KeyEvent event) {
//                System.out.println("KEY PRESSED: " + event.getID());
//
//                switch (event.getID()) {
//                    case KeyEvent.KEY_PRESSED:
//                        if (event.getKeyCode() == KeyEvent.VK_PLUS) {
//                            AtlantisGame.changeSpeedBy(-2);
//                        }
//                        break;
//
//                    case KeyEvent.KEY_RELEASED:
//                        if (event.getKeyCode() == KeyEvent.VK_MINUS) {
//                            AtlantisGame.changeSpeedBy(+2);
//                        }
//                        break;
//                }
//                return false;
//            }
//
//        });
}
