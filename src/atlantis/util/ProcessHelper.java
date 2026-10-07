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
        if (Env.isWine()) {
            killWineGameProcesses();
            return;
        }
        if (Env.isOpenBW()) {
            killOpenBWProcesses();
            return;
        }
        executeInCommandLine("taskkill /IM StarCraft.exe /T /F");
    }

    public static void killChaosLauncherProcess() {
        if (Env.isWine()) {
            killWineGameProcesses();
            return;
        }
        if (Env.isOpenBW()) {
            killOpenBWProcesses();
            return;
        }
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
        killWineGameProcesses();
        // wineserver must die too: it holds the whole Wine session (and after
        // an Escape the owner wants nothing left). The sleep below gives X a
        // moment to tear the windows down before anything starts again.
        executeInCommandLine("wineserver -k");
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Kills the Wine game host (StarCraft and ChaosLauncher) without waiting.
     * Shared by the two platform-specific kill surfaces so neither of them can
     * shell out to {@code taskkill} on Linux - a call that does not exist there
     * and used to end the exit path with
     * {@code java.io.IOException: Cannot run program "taskkill"}.
     */
    private static void killWineGameProcesses() {
        // "Chaoslauncher.exe" is 17 characters - longer than the kernel's 15
        // character comm limit, so "pkill -x Chaoslauncher.exe" never matches
        // anything (measured 2026-10-06). Without -x the pattern is matched
        // against comm only, which truncates to 15 characters too, so
        // "pkill -9 Chaoslauncher" is the working form for that process.
        executeInCommandLine("pkill -9 -x StarCraft.exe");
        executeInCommandLine("pkill -9 Chaoslauncher");
    }

    /**
     * Wine-only cleanup, called from the game-exit path. Kills the game host
     * and the whole virtual desktop, so no {@code wineserver} is left holding
     * the {@code scgame} window after Escape or after the game ends.
     */
    public static void killWineProcessesIfOnWine() {
        if (!Env.isWine()) return;

        killWineProcesses();
        executeInCommandLine("wineserver -k");
    }

    /**
     * Starts ChaosLauncher inside a Wine virtual desktop of the configured
     * size. The virtual desktop protects the host: StarCraft 1.16.1 run bare
     * switches the X screen resolution and resets HiDPI scaling on exit.
     *
     * <p>The desktop is configured in the Wine registry (see
     * {@link WineWindowConfig#enableVirtualDesktopCommands()}), not with
     * {@code wine explorer /desktop=}, because the latter produces a window the
     * window manager maximizes and no tool can resize afterwards. The registry
     * way creates an ordinary window of the requested size.</p>
     */
    public static void startChaosLauncherUnderWine() {
        // W-MODE owns the game window on Wine (it is the only thing here that
        // can scale the game, and the owner's test showed the alternative -
        // the Wine virtual desktop - gives an unusably small 640x480 game).
        // enableWModeCommands turns the virtual desktop off and the plugin on;
        // the virtual-desktop setup below is kept for the record but unused.
        for (String command : WineWindowConfig.enableWModeCommands()) {
            executeInCommandLine(command);
        }
        writeWModeIni();

        // wineserver needs a moment to pick up the registry change above.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        String gameRoot = wineGameRoot();
        String chaos = gameRoot + "/chaoslauncher/Chaoslauncher.exe";

        String command = String.format(
            "cd %s && DISPLAY=%s wine \"%s\"",
            gameRoot,
            displayEnv(),
            chaos
        );

        System.out.println("[Atlantis] Launching: " + command);
        executeInCommandLineDetached(command);

        positionWineWindowWhenReady();
    }

    /**
     * Writes {@code C:\sc\wmode.ini} so W-MODE starts with our geometry
     * instead of leftovers from a previous manual run. The file lives in the
     * game root, not in {@code bwapi-data}.
     */
    private static void writeWModeIni() {
        try {
            java.io.File ini = new java.io.File(wineGameRoot(), "wmode.ini");
            java.nio.file.Files.write(
                ini.toPath(),
                WineWindowConfig.wmodeIniContent().getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
            System.out.println("[Atlantis] Wrote W-MODE config: " + ini.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[Atlantis] Failed to write wmode.ini: " + e.getMessage());
        }
    }

    /**
     * The desktop window takes a few seconds to appear, and a position set too
     * early lands on nothing. There is no event to wait for, so this polls
     * {@code wmctrl -x -l} for the window title up to ~8 s and positions it the
     * moment it is there. Runs on a daemon thread so the bot can keep starting
     * the game while the window is being placed.
     */
    private static void positionWineWindowWhenReady() {
        final String[] command = WineWindowConfig.positionWindowCommand();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!Env.isWine()) return;
                String title = WineWindowConfig.DESKTOP_NAME + " - Wine desktop";

                for (int attempt = 0; attempt < 80; attempt++) {
                    if (wmctrlSeesWindow(title)) {
                        executeDetached(command);
                        return;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                        return;
                    }
                }
            }
        }, "wine-window-position");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Runs an already-split command vector without waiting. Used for the window
     * positioner, whose script contains shell builtins ({@code eval},
     * {@code command}) that only exist inside a shell - running the vector
     * through {@code Runtime.exec(String)} word-splits it and tries to execute
     * {@code eval} as a program (measured 2026-10-06: {@code IOException:
     * Cannot run program "eval"}).
     */
    private static void executeDetached(String[] command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    private static boolean wmctrlSeesWindow(String title) {
        try {
            Process process = new ProcessBuilder("wmctrl", "-x", "-l").redirectErrorStream(true).start();
            String output = readAll(process.getInputStream());
            process.waitFor();
            return output != null && output.contains(title);
        } catch (Exception e) {
            // wmctrl missing is not fatal - the WM places the window, as before.
            return false;
        }
    }

    private static String readAll(java.io.InputStream stream) throws java.io.IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int read;
        while ((read = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return new String(buffer.toByteArray(), "UTF-8");
    }

    /**
     * Kills the bot's own JVM running under Wine, if one is up - the client
     * half of the Wine setup. From the Linux supervisor JVM there is no way to
     * name it directly (it is a Windows process inside Wine), so its command
     * line is matched instead: the only java.exe this project launches runs
     * the Atlantis jar.
     */
    public static void killWineClientJvm() {
        executeInCommandLine("pkill -9 -f 'java.exe.*Atlantis.jar'");
    }

    /**
     * Kills the OpenBW backend's processes: the game host (BWAPILauncher, the
     * headless engine) and any leftover shared state. Called both at startup
     * (a stale host would otherwise be adopted by the client and loop on a
     * dead PID) and at exit - the OpenBW twin of the Wine/Windows kill pair.
     *
     * <p>Uses {@code pkill -9 -x} (exact name): {@code -f} with a loose
     * pattern also matches the killing shell's own command line (measured
     * 2026-10-05).</p>
     */
    public static void killOpenBWProcesses() {
        executeInCommandLine("pkill -9 -x BWAPILauncher");
        executeInCommandLine("sh -c 'rm -f /dev/shm/bwapi_shared_memory_* /tmp/bwapi_socket_*'");
    }

    /**
     * True when an OpenBW host is already running.
     *
     * <p>Used by {@code OpenBWGameLauncher} to decide whether a cleanup is
     * needed: killing a live host is what breaks the attach, because the host
     * is the one that owns the shared-memory game table and the socket this
     * client is about to join (measured 2026-10-07).
     */
    public static boolean isOpenBWProcessRunning() {
        try {
            Process p = new ProcessBuilder("pgrep", "-x", "BWAPILauncher")
                    .redirectErrorStream(true)
                    .start();
            p.waitFor();
            return p.exitValue() == 0;
        } catch (Exception e) {
            // No pgrep (non-Linux) or it failed: assume nothing is running, so
            // the caller falls back to the old cleanup behaviour.
            return false;
        }
    }

    /**
     * Force-kills the whole Wine session: the game, the launcher and the
     * wineserver. Runs through {@code sh} on the HOST, so it works both from
     * the Linux supervisor JVM and from the bot's JVM running under Wine (Wine
     * passes unknown executables through to the host).
     */
    public static void killWineHostProcesses() {
        String[] commands = {
            "pkill -9 -x StarCraft.exe",
            "pkill -9 Chaoslauncher",
            "wineserver -k",
        };
        for (String command : commands) {
            try {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
            } catch (Exception ignored) {
            }
        }
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
