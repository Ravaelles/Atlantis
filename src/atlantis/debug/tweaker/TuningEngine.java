package atlantis.debug.tweaker;

/**
 * One game engine the tuner loop can play on (NEXT #160, step 6).
 * The loop asks the engine instead of hardcoding paths: which batch
 * script runs N games, which base env file the games read, how long a
 * game may take, and whether per-game param overrides actually reach
 * the bot on this engine.
 *
 * <p>Wine is the only fully tuning-capable engine today (per-game env
 * files plus the in-JVM tweaker). OpenBW writes no tweaker rows - the
 * client is killed on timeout before OnGameEnd fires - and the duel
 * map does not tick there, so it stays a descriptor until the runner
 * side lands. The sim has no batch runner yet at all.
 */
public abstract class TuningEngine {

    /** CLI name, e.g. {@code wine}. */
    public abstract String name();

    /** Batch script playing N games into an OUTDIR, wine-batch-shaped. */
    public abstract String batchScript();

    /** Base env file the batch copies per game. */
    public abstract String defaultEnv();

    /** Wall-clock seconds a single game may take on this engine. */
    public abstract int gameTimeoutSeconds();

    /** False when the engine cannot play a game at all right now. */
    public abstract boolean isLive();

    /**
     * True when per-game KEY=VALUE overrides reach the bot and the
     * played values come back readable (tweaker rows). False means the
     * loop cannot tune here yet, only play.
     */
    public abstract boolean supportsTuning();
}
