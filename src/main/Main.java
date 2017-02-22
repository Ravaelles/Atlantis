package main;

import atlantis.Atlantis;
import atlantis.keyboard.AKeyboard;
import atlantis.util.ProcessHelper;

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
        
        // Kill previous Starcraft.exe process
        ProcessHelper.killStarcraftProcess();
        
        // Kill previous Chaoslauncher.exe process
        ProcessHelper.killChaosLauncherProcess();
        
        // Autostart Chaoslauncher
        // Combined with Chaoslauncher -> Settings -> Run Starcraft on Startup 
        // SC will be autostarted at this moment
        ProcessHelper.startChaosLauncherProcess();

        // =============================================================
        // =============================================================
        // ==== See AtlantisConfig class to customize execution ========
        // =============================================================
        // =============================================================
        //
        // Create Atlantis object to use for this bot. It wraps BWMirror functionality.
        Atlantis atlantis = new Atlantis();
        
        // Listen for keyboard events
        AKeyboard.listenForKeyEvents();

        // Starts bot.
        atlantis.run();
    }

}
