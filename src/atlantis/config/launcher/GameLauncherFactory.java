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
        return new ChaosGameLauncher();
    }
}
