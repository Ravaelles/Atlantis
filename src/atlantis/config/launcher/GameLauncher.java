package atlantis.config.launcher;

/**
 * Strategy for starting the game environment before the bot logic runs.
 *
 * <p>Atlantis historically only knew one way to play: Windows + ChaosLauncher
 * injecting BWAPI into a live StarCraft process. The OpenBW backend
 * (StardustDevEnvironment, Linux, headless) inverts that model: an external
 * server ({@code BWAPILauncher}) hosts the game and the bot only attaches
 * via {@code BWClient.startGame()}. Each backend gets its own implementation;
 * game logic never branches on the backend (Open/Closed Principle).</p>
 */
public interface GameLauncher {

    /**
     * Prepares everything the chosen backend needs (map selection, processes,
     * config files) and returns. The bot itself is started afterwards by the
     * caller via {@code Atlantis.run()}.
     */
    void launch(String[] args);
}
