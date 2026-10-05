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

        warnIfMapMissing(ActiveMap.name());

        // Escape must kill the game host (Wine) and then the JVM - the same
        // "Escape quits everything" the Windows setup had. JNativeHook ships a
        // Linux x86_64 native library, so the hook works on the X display Wine
        // runs on.
        AKeyboard.listenForKeyEvents();

        AtlantisIgniter.modifyBwapiFileIfNeeded();
        ProcessHelper.startChaosLauncherUnderWine();
    }

    /**
     * A map name that does not exist in the Wine install is the silent failure
     * this backend is most prone to: BWAPI cannot load the map, the game never
     * starts, and the bot prints "Game table mapping not found" forever while
     * the player sits in the StarCraft menu. Say it out loud instead.
     */
    private void warnIfMapMissing(String mapName) {
        if (mapName == null) return;

        String prefix = System.getenv("WINEPREFIX");
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = System.getProperty("user.home") + "/.wine";
        }

        String mapsRoot = prefix + "/drive_c/sc/maps/";
        String candidate = mapName.startsWith("maps/") ? mapName : "maps/" + mapName;

        // The exact path matters, not just the file name: a map that exists
        // under a different subfolder (e.g. ums/rav/minimaps/) is still not
        // loadable at the path written into bwapi.ini.
        File exact = new File(prefix + "/drive_c/sc/" + candidate);
        if (new File(mapsRoot).exists() && !exact.exists()) {
            String fileName = candidate.substring(candidate.lastIndexOf('/') + 1);
            String foundUnder = findMapFolder(new File(mapsRoot), fileName);

            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("[Atlantis] WARNING: map not found at the exact path:");
            System.out.println("[Atlantis]   " + candidate);
            if (foundUnder != null) {
                System.out.println("[Atlantis]   a file of that name exists under: " + foundUnder);
                System.out.println("[Atlantis]   pass --map=<that path relative to maps/> instead.");
            } else {
                System.out.println("[Atlantis]   no file of that name anywhere under " + mapsRoot);
            }
            System.out.println("[Atlantis]   the game will NOT start; the bot would sit in the menu.");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    /** Returns the folder (relative to maps/) holding a file of this name, or null. */
    private String findMapFolder(File dir, String fileName) {
        return findMapFolder(dir, fileName, dir, 0);
    }

    private String findMapFolder(File dir, String fileName, File root, int depth) {
        if (depth > 6) return null;

        File[] children = dir.listFiles();
        if (children == null) return null;

        for (File child : children) {
            if (child.isDirectory()) {
                String found = findMapFolder(child, fileName, root, depth + 1);
                if (found != null) return found;
            } else if (child.getName().equalsIgnoreCase(fileName)) {
                String full = child.getParentFile().getPath();
                String rootPath = root.getPath();
                String relative = full.startsWith(rootPath) ? full.substring(rootPath.length()) : full;
                return "maps" + relative;
            }
        }
        return null;
    }
}