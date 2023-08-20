package main;

import atlantis.Atlantis;
import atlantis.config.AtlantisIgniter;
import atlantis.config.env.Env;
import atlantis.keyboard.AKeyboard;
import atlantis.util.ProcessHelper;

/**
 * This is the main class of the bot. Here everything starts.
 * <p>
 * "A journey of a thousand miles begins with a single step." - Lao Tse
 */
public class Main {
    /**
     * Sets up Atlantis config and runs the bot.
     */
    public static void main(String[] args) {
        Env.readEnvFile(args);

        // If run locally (not in tournament) auto-start Starcraft.exe and Chaoslauncher, modify bwapi.ini etc
        if (Env.isLocal()) {
            ProcessHelper.killStarcraftProcess();
            ProcessHelper.killChaosLauncherProcess();

            // Dynamically modify bwapi.ini file, change race and enemy race.
            // If you want to change your/enemy race, edit AtlantisRaceConfig constants.
            AtlantisIgniter.modifyBwapiFileIfNeeded();

            // Listen for keyboard events
            AKeyboard.listenForKeyEvents();

            // IMPORTANT: Make sure Chaoslauncher -> Settings -> "Run Starcraft on Startup" i1s checked
            ProcessHelper.startChaosLauncherProcess();
        }

        // =============================================================
        // =============================================================
        // ==== See AtlantisRaceConfig class to customize execution ========
        // =============================================================
        // =============================================================

        // Create Atlantis object to use for this bot. It wraps BWMirror functionality.
        Atlantis atlantis = new Atlantis();

        // Starts bot.
        atlantis.run();
    }
}
