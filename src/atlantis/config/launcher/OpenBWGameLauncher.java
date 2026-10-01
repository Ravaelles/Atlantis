package atlantis.config.launcher;

import atlantis.config.ActiveMap;
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
    }
}
