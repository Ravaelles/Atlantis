package atlantis.config.launcher;

import atlantis.config.ActiveMap;
import atlantis.config.AtlantisIgniter;
import atlantis.keyboard.AKeyboard;
import atlantis.util.ProcessHelper;
import main.Main;

/**
 * Original Windows backend: live StarCraft + ChaosLauncher.
 *
 * <p>Behavior is byte-for-byte the old {@code Main.localAtlantisSetup}:
 * pick map, hook keyboard, kill leftovers, patch {@code bwapi.ini},
 * start ChaosLauncher (which in turn autostarts StarCraft).</p>
 */
public class ChaosGameLauncher implements GameLauncher {

    @Override
    public void launch(String[] args) {
        ActiveMap.specifyMap(Main.defineMapToUse(args));

        AKeyboard.listenForKeyEvents();

        ProcessHelper.killStarcraftProcess();
        ProcessHelper.killChaosLauncherProcess();

        // Dynamically modify bwapi.ini file, change race and enemy race.
        // If you want to change your/enemy race, edit AtlantisRaceConfig constants.
        AtlantisIgniter.modifyBwapiFileIfNeeded();

        // IMPORTANT: Make sure Chaoslauncher -> Settings -> "Run Starcraft on Startup" is checked
        ProcessHelper.startChaosLauncherProcess();
    }
}
