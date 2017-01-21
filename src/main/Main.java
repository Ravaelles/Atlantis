package main;

import atlantis.Atlantis;
import atlantis.keyboard.AtlantisKeyboard;

/**
 * This is the main class of the bot. Here everything starts.
 *
 * "A journey of a thousand miles begins with a single step." - Lao Tse
 */
public class Main {

    /**
     * Sets up Atlantis config and runs the bot.
     */
    public static void main(String[] args) {

        // =============================================================
        // =============================================================
        // ==== See AtlantisConfig class to customize execution ========
        // =============================================================
        // =============================================================
        //
        // Create Atlantis object to use for this bot. It wraps BWMirror functionality.
        Atlantis atlantis = new Atlantis();
        
        // Listen for keyboard events
        AtlantisKeyboard.listenForKeyEvents();

        // Starts bot.
        atlantis.run();
    }

}
