package atlantis.config.launcher;

import atlantis.config.env.Env;

/**
 * Picks the {@link GameLauncher} for the current run.
 *
 * <p>Single decision point (no backend {@code if}-s scattered across the
 * codebase). Defaults to ChaosLauncher so existing Windows setups keep
 * working untouched. {@code GAME_LAUNCHER} in {@code bwapi-data/AI/ENV}
 * switches backends:</p>
 * <ul>
 *   <li>{@code CHAOS} (default) - Windows, native StarCraft + ChaosLauncher;</li>
 *   <li>{@code OPENBW} - Linux, headless OpenBW server;</li>
 *   <li>{@code WINE} - Linux, real StarCraft + ChaosLauncher under Wine.</li>
 * </ul>
 */
public final class GameLauncherFactory {

    private GameLauncherFactory() {
    }

    public static GameLauncher forCurrentEnv() {
        if (Env.isOpenBW()) {
            return new OpenBWGameLauncher();
        }
        if (Env.isWine()) {
            return new UnixChaosGameLauncher();
        }
        if (Env.isWineClient()) {
            // WINECLIENT means "this JVM is the bot" - true only when the JVM
            // itself runs under Wine (a Windows process). A Linux JVM (the IDE)
            // selecting WINECLIENT would just loop on "Game table mapping not
            // found" forever, because JBWAPI picks the POSIX connection backend
            // and never sees the Wine-side shared memory (measured 2026-10-06).
            // So the IDE is routed to the supervisor launcher instead, which
            // runs scripts/run-wine-full.sh and exits.
            return runningUnderWine()
                ? new WineClientGameLauncher()
                : new UnixChaosGameLauncher();
        }
        return new ChaosGameLauncher();
    }

    /**
     * True when this JVM itself is a Windows process running under Wine -
     * detected via the env markers Wine sets for its child processes (the same
     * check {@code UnixChaosGameLauncher} uses).
     */
    private static boolean runningUnderWine() {
        return System.getenv("WINEDEBUG") != null
            || System.getenv("WINEDLLOVERRIDES") != null
            || System.getenv("WINEPREFIX") != null;
    }
}
