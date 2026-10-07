package atlantis.config.launcher;

import atlantis.config.ActiveMap;
import atlantis.keyboard.AKeyboard;
import atlantis.util.ProcessHelper;
import main.Main;

/**
 * Linux backend: OpenBW server hosted outside this process.
 *
 * <p>Unlike {@link ChaosGameLauncher} this strategy deliberately does
 * <b>not</b>:</p>
 * <ul>
 *   <li>start or kill any Windows process ({@code taskkill}, {@code cmd /c}),</li>
 *   <li>hook the keyboard ({@code AKeyboard} exits the JVM when the native
 *   hook is unavailable, e.g. on headless Linux),</li>
 *   <li>patch {@code bwapi.ini} (races, map and game type are owned by the
 *   OpenBW server — see {@code BWAPILauncher} / {@code bwapi.ini} on the
 *   server side).</li>
 * </ul>
 *
 * <p>After this method returns, the caller runs {@code Atlantis.run()},
 * whose {@code BWClient.startGame()} blocks until the OpenBW server hosts a
 * game and then attaches to it as a BWAPI client (shared memory, POSIX).</p>
 */
public class OpenBWGameLauncher implements GameLauncher {

    @Override
    public void launch(String[] args) {
        String map = Main.defineMapToUse(args);
        ActiveMap.specifyMap(map);

        System.out.println("===============================================");
        System.out.println("[Atlantis] Backend: OpenBW (Linux, headless).");
        System.out.println("[Atlantis] Requested map (advisory, server owns the game): " + map);
        System.out.println("[Atlantis] Waiting for the OpenBW server (BWAPILauncher)");
        System.out.println("[Atlantis] to host a game - see StardustDevEnvironment");
        System.out.println("[Atlantis] DOCS/HOW-ATLANTIS-OPENBW.md for the recipe.");
        System.out.println("===============================================");

        // A leftover host is poison (a stale BWAPILauncher owns the game table,
        // so this client adopts its PID and loops on "Unable to open shared
        // memory mapping" against a dead segment - measured 2026-10-06, PID
        // 647373). But this launcher must NOT kill a host that is up and
        // serving: the whole point of this backend is that someone else starts
        // the game and the bot attaches to it, and an unconditional
        // `pkill -9 -x BWAPILauncher` here kills the very host the client is
        // about to join (measured 2026-07-10: the launcher's own kill ran
        // between "host ready" and "client connects", and the client then
        // reported "Unable to open communications socket" against a host it had
        // just murdered). Kill only when nothing is hosting, and clear stale
        // shared-memory names only then too.
        if (!ProcessHelper.isOpenBWProcessRunning()) {
            ProcessHelper.killOpenBWProcesses();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }

        // The Linux keyboard hook (JNativeHook ships an x86_64 native) gives
        // this setup the same global Esc the Windows path has: kill everything.
        // It used to be skipped entirely in this launcher, which is why Escape
        // did nothing on OpenBW (measured 2026-10-06). Registration must not
        // fail the JVM - AKeyboard keeps going when the hook is unavailable.
        AKeyboard.listenForKeyEvents();
    }
}
