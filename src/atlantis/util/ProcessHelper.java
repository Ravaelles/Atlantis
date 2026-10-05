package atlantis.util;

import atlantis.config.AtlantisIgniter;
import atlantis.config.env.Env;
import atlantis.config.launcher.WineWindowConfig;

import java.io.File;

/**
 * Kills and starts processes: the Starcraft game itself and Chaoslauncher on
 * Windows, the same pair under Wine on Linux.
 *
 * <p>Two platforms, two mechanisms, one job: Windows uses {@code taskkill} and
 * {@code cmd /c}, Linux uses {@code pkill} and {@code wine}. The launcher
 * strategy ({@code ChaosGameLauncher} vs {@code UnixChaosGameLauncher}) picks
 * the pair; nothing else branches on the platform.</p>
 */
public class ProcessHelper {

    // =========================================================
    // Windows (native) ========================================

    public static void killStarcraftProcess() {
        executeInCommandLine("taskkill /IM StarCraft.exe /T /F");
    }

    public static void killChaosLauncherProcess() {
        executeInCommandLine("taskkill /IM Chaoslauncher.exe /T /F");
    }

    /**
     * Autostart Chaoslauncher
     * Combined with Chaoslauncher -> Settings -> Run Starcraft on Startup
     * SC will be autostarted at this moment
     */
    public static void startChaosLauncherProcess() {
        try {
            Thread.sleep(150);
            String command = "cmd /c " + AtlantisIgniter.getChaosLauncherPath();

            executeInCommandLine(command);
        } catch (InterruptedException ignored) {
        }
    }

    // =========================================================
    // Linux (Wine) ============================================

    /**
     * Kills any leftover StarCraft / ChaosLauncher / wineserver. A stale Wine
     * process holds the game window and the BWAPI shared state, and the next
     * run then attaches to a dead game - the Linux twin of a stale
     * {@code taskkill} being skipped on Windows.
     *
     * <p>{@code -x} matches the exact process name on purpose: {@code pkill -f}
     * with a loose pattern also matches the killing shell's own command line
     * (cost: a truncated command chain, measured 2026-10-05).</p>
     */
    public static void killWineProcesses() {
        executeInCommandLine("pkill -9 -x StarCraft.exe");
        executeInCommandLine("pkill -9 -x Chaoslauncher.exe");
        // Give the window/X a moment to disappear before starting again.
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Starts ChaosLauncher inside a Wine virtual desktop. The virtual desktop
     * is what protects the host: StarCraft 1.16.1 otherwise switches the X
     * screen resolution and resets HiDPI scaling on exit.
     */
    public static void startChaosLauncherUnderWine() {
        String gameRoot = wineGameRoot();
        String chaos = gameRoot + "/chaoslauncher/Chaoslauncher.exe";

        String command = String.format(
            "cd %s && DISPLAY=%s wine explorer %s \"%s\"",
            gameRoot,
            displayEnv(),
            WineWindowConfig.desktopArgument(),
            chaos
        );

        System.out.println("[Atlantis] Launching: " + command);
        executeInCommandLineDetached(command);
    }

    private static String wineGameRoot() {
        String prefix = System.getenv("WINEPREFIX");
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = System.getProperty("user.home") + "/.wine";
        }
        return prefix + "/drive_c/sc";
    }

    private static String displayEnv() {
        String display = System.getenv("DISPLAY");
        return (display == null || display.trim().isEmpty()) ? ":0" : display;
    }

    // =========================================================

    private static void executeInCommandLine(String command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    /**
     * Runs through {@code sh -c} so a compound command (cd && wine ...) works,
     * and does not wait for the game to exit - the bot has to keep running.
     */
    private static void executeInCommandLineDetached(String command) {
        try {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
