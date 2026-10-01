package atlantis.config.launcher;

import atlantis.config.env.Env;

/**
 * Picks the {@link GameLauncher} for the current run.
 *
 * <p>Single decision point (no backend {@code if}-s scattered across the
 * codebase). Defaults to ChaosLauncher so existing Windows setups keep
 * working untouched; {@code GAME_LAUNCHER=OPENBW} in {@code bwapi-data/AI/ENV}
 * switches to the OpenBW backend.</p>
 */
public final class GameLauncherFactory {

    private GameLauncherFactory() {
    }

    public static GameLauncher forCurrentEnv() {
        if (Env.isOpenBW()) {
            return new OpenBWGameLauncher();
        }
        return new ChaosGameLauncher();
    }
}
