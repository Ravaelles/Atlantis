package atlantis.config.launcher;

import atlantis.config.ActiveMap;
import atlantis.config.AtlantisIgniter;
import atlantis.config.env.Env;
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

        // The JVM itself decides this from the environment: if we are running
        // under Wine (JAVA being a Windows exe), the game host is already up -
        // the owner started it - and this process is a client that only needs
        // to attach. Starting a second ChaosLauncher or pkill-ing Linux-side
        // (no pkill exists inside Wine) would both be wrong here.
        if (runningUnderWine()) {
            System.out.println("===============================================");
            System.out.println("[Atlantis] Backend: Wine client (JVM under Wine, attaching to the running game).");
            System.out.println("[Atlantis] Map: " + ActiveMap.name());
            System.out.println("===============================================");

            warnIfMapMissing(ActiveMap.name());

            AtlantisIgniter.modifyBwapiFileIfNeeded();
            return; // Do NOT start ChaosLauncher, do NOT pkill.
        }

        System.out.println("===============================================");
        System.out.println("[Atlantis] Backend: Wine + StarCraft + ChaosLauncher (Linux).");
        System.out.println("[Atlantis] Map: " + ActiveMap.name());
        System.out.println("[Atlantis] Game window: " + WineWindowConfig.describe());
        System.out.println("===============================================");

        warnIfMapMissing(ActiveMap.name());

        // This JVM is the IDE's Linux JVM - it can never BE the bot (JBWAPI
        // would select the POSIX connection backend and never see the Wine-side
        // BWAPI shared memory; measured 2026-10-06). Instead it acts as the
        // supervisor: run the verified script, stream its output into this
        // console so the IDE shows the progress, and exit when it finishes.
        // The real bot is the Windows-JRE JVM the script starts under Wine;
        // production/tournament is untouched - it always took the Windows
        // ChaosGameLauncher path.
        runWineFullScript(ActiveMap.name());
    }

    /**
     * Runs {@code scripts/run-wine-full.sh} (client first, then the game,
     * watches for HELLO_WORLD) with inherited I/O so the owner sees everything
     * in the IDE console, and ends this JVM with the script's exit code.
     */
    private static void runWineFullScript(String mapName) {
        String script = "scripts/run-wine-full.sh";
        if (!new File(script).exists()) {
            script = "/sc-ai/Atlantis/scripts/run-wine-full.sh";
        }

        ProcessBuilder pb = new ProcessBuilder("bash", script, mapName == null ? "" : mapName);
        pb.inheritIO();

        try {
            int exitCode = pb.start().waitFor();
            System.out.println("[Atlantis] run-wine-full.sh finished with exit code " + exitCode);
            System.exit(exitCode);
        } catch (Exception e) {
            System.err.println("[Atlantis] Failed to run " + script + ": " + e.getMessage());
            System.err.println("[Atlantis] Run it by hand: bash scripts/run-wine-full.sh");
            System.exit(1);
        }
    }

    /**
     * True when this JVM itself is a Windows process running under Wine -
     * i.e. the owner started the bot with {@code wine java -jar Atlantis.jar}.
     * Detected via the {@code winelauncher} environment marker Wine sets; the
     * absence of /proc is not used on purpose, since that file also exists in
     * some containers.
     */
    private static boolean runningUnderWine() {
        return System.getenv("WINEDEBUG") != null || System.getenv("WINEDLLOVERRIDES") != null;
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