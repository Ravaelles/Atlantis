package atlantis.config.launcher.openbw;

import atlantis.config.ActiveMap;
import atlantis.util.log.ErrorLog;

/**
 * The programmatic entry point for a headless OpenBW game - the API that makes
 * a
 * run readable at the call site, in Main and in tests alike.
 *
 * <p>
 * Usage, in full:
 * </p>
 *
 * <pre>
 * OpenBWGameLauncher.init()
 *         .useConfig(OpenBWConfig.ofMap(Main.defineMapToUse(args)))
 *         .race("Protoss", "Zerg")
 *         .run();
 * </pre>
 *
 * <p>
 * {@link #run()} hosts the game and <b>blocks until the bot has played</b>; it
 * returns a verdict rather than throwing, so a caller (a test, a script,
 * {@code Main}) decides what to do with a failure.
 * </p>
 */
public final class OpenBWGameLauncher {

    private final OpenBWConfig.Builder config;

    private OpenBWGameLauncher(OpenBWConfig initial) {
        this.config = initial != null
                ? OpenBWConfig.builder()
                        .harnessDir(initial.harnessDir())
                        .gameDir(initial.gameDir())
                        .serverScript(initial.serverScript())
                        .map(initial.map())
                        .races(initial.ourRace(), initial.enemyRace())
                        .logDir(initial.logDir())
                        .gameTimeoutSeconds(initial.gameTimeoutSeconds())
                        .hostWaitSeconds(initial.hostWaitSeconds())
                : OpenBWConfig.builder();
    }

    /** Starts a run with this machine's defaults. */
    public static OpenBWGameLauncher init() {
        return new OpenBWGameLauncher(null);
    }

    public static OpenBWGameLauncher init(OpenBWConfig config) {
        return new OpenBWGameLauncher(config);
    }

    /** Replaces the whole configuration. */
    public OpenBWGameLauncher useConfig(OpenBWConfig value) {
        return new OpenBWGameLauncher(value);
    }

    public OpenBWGameLauncher map(String map) {
        config.map(map);
        return this;
    }

    public OpenBWGameLauncher race(String ours, String enemy) {
        config.races(ours, enemy);
        return this;
    }

    public OpenBWGameLauncher logDir(String dir) {
        config.logDir(dir);
        return this;
    }

    public OpenBWGameLauncher gameTimeoutSeconds(int seconds) {
        config.gameTimeoutSeconds(seconds);
        return this;
    }

    public OpenBWGameLauncher hostWaitSeconds(int seconds) {
        config.hostWaitSeconds(seconds);
        return this;
    }

    public OpenBWConfig config() {
        return config.build();
    }

    /**
     * Hosts the game and plays the configured run to completion.
     *
     * @return the verdict: whether the client attached, the last frame seen, and
     *         the log paths - the same facts the shell script used to grep for.
     */
    public OpenBWRunResult run() {
        OpenBWConfig resolved = config.build();

        // The map the bot asks for must be the map the harness hosts; a mismatch
        // is silent (the bot analyses a map it is not playing).
        ActiveMap.specifyMap(resolved.map());

        return new OpenBWRunner(resolved).run();
    }

    /** Fails fast, with the reason, when this machine cannot host OpenBW at all. */
    public boolean canRun() {
        String missing = config.build().missingRequirement();
        if (missing != null) {
            ErrorLog.printPlusToFile("OpenBW cannot run here: " + missing);
            return false;
        }
        return true;
    }
}
