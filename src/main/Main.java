package main;

import atlantis.Atlantis;
import atlantis.AtlantisIgniter;
import atlantis.env.Env;
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
        Env.readEnvFile(args);

        // If run locally (not in tournament) auto-start Starcraft.exe and do other stuff
        if (Env.isLocal()) {
            ProcessHelper.killStarcraftProcess();
            ProcessHelper.killChaosLauncherProcess();

            if (Env.isLocal()) {

                // Dynamically modify bwapi.ini file, change race and enemy race.
                // If you want to change your/enemy race, edit AtlantisConfig constants.
                AtlantisIgniter.modifyBwapiFileIfNeeded();
            }

            // Listen for keyboard events
            AKeyboard.listenForKeyEvents();

            // IMPORTANT: Make sure Chaoslauncher -> Settings -> "Run Starcraft on Startup" i1s checked
            ProcessHelper.startChaosLauncherProcess();
        }

        // =============================================================
        // =============================================================
        // ==== See AtlantisConfig class to customize execution ========
        // =============================================================
        // =============================================================

        // Create Atlantis object to use for this bot. It wraps BWMirror functionality.
        Atlantis atlantis = new Atlantis();

        // Starts bot.
        atlantis.run();
    }

}
