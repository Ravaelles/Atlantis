package atlantis.config.launcher;

import atlantis.config.ActiveMap;
import atlantis.config.AtlantisIgniter;
import atlantis.config.env.Env;
import atlantis.keyboard.AKeyboard;
import atlantis.util.ProcessHelper;
import main.Main;

import java.io.File;

/**
 * Linux backend: real StarCraft 1.16.1 + BWAPI 4.4.0 + ChaosLauncher under
 * Wine, with the native Blizzard AI as the opponent. This is the Linux
 * equivalent of the Windows "F5 from IntelliJ" workflow: one bot start brings
 * the game up and the bot plays it.
 *
 * <p>
 * Differs from {@link ChaosGameLauncher} (Windows) only where the platform
 * does: Wine instead of a native {@code StarCraft.exe}, {@code wine explorer
 * /desktop} instead of the Windows desktop, {@code pkill} instead of
 * {@code taskkill}. The race/map/bwapi.ini patching is shared - the game is the
 * same game, and {@code bwapi.ini} is still what configures it.
 * </p>
 *
 * <p>
 * Paths come from {@code ENV} (see {@link Env}):
 * </p>
 * 
 * <pre>
 *   STARCRAFT_DIR   = /sc-ai/starcraft
 *   BWAPI_DIST_DIR  = /sc-ai/BWAPI          (BWAPI 4.4.0 distribution)
 *   WINE_PREFIX     = ~/.wine               (optional)
 *   WINE_WINDOW_*   = x/y/width/height      (see {@link WineWindowConfig})
 * </pre>
 *
 * <p>
 * The game runs inside a Wine <b>virtual desktop</b> so that StarCraft's
 * resolution switch never touches the host desktop (measured: it resets a
 * HiDPI 200% scale to 100%).
 * </p>
 */
public class UnixChaosGameLauncher implements GameLauncher {

    @Override
    public void launch(String[] args) {
        ActiveMap.specifyMap(Main.defineMapToUse(args));

        System.out.println("===============================================");
        System.out.println("[Atlantis] Backend: Wine + StarCraft + ChaosLauncher (Linux).");
        System.out.println("[Atlantis] Map: " + ActiveMap.name());
        System.out.println("[Atlantis] Game window: " + WineWindowConfig.describe());
        System.out.println("===============================================");

        ProcessHelper.killWineProcesses();

        // Escape must kill the game host (Wine) and then the JVM - the same
        // "Escape quits everything" the Windows setup had. JNativeHook ships a
        // Linux x86_64 native library, so the hook works on the X display Wine
        // runs on.
        AKeyboard.listenForKeyEvents();

        AtlantisIgniter.modifyBwapiFileIfNeeded();
        ProcessHelper.startChaosLauncherUnderWine();
    }
}