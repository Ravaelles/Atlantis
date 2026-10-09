package atlantis.config.launcher.openbw;

import atlantis.util.AFile;

/**
 * Everything an OpenBW run needs to know, as a value: where the harness lives,
 * which map and races to play, and where the logs go. Defaults match this
 * machine; a caller overrides only what it cares about.
 *
 * <p>
 * This is deliberately a plain value object with {@code with*} copies: the
 * launcher API is then readable at the call site and testable without a game.
 * </p>
 */
public final class OpenBWConfig {

    /** The harness build tree (BWAPILauncher, libs, maps). */
    public static final String DEFAULT_HARNESS_DIR = "/sc-ai/StardustDevEnvironment";

    /** The working directory the harness resolves maps and MPQs from. */
    public static final String DEFAULT_GAME_DIR = DEFAULT_HARNESS_DIR + "/build/test";

    /**
     * The documented host script; its PID is the host's PID (it ends with exec).
     */
    public static final String DEFAULT_SERVER_SCRIPT = DEFAULT_HARNESS_DIR + "/scripts/run-openbw-server.sh";

    /** The owner's map; the harness ships it under maps/cog/. */
    public static final String DEFAULT_MAP = "maps/cog/(3)TauCross1.1.scx";

    public static final String DEFAULT_OUR_RACE = "Protoss";
    public static final String DEFAULT_ENEMY_RACE = "Zerg";

    /**
     * The OpenBW simulation duration is capped at two minutes (CONVENTIONS §17).
     * The name is kept for compatibility; TIMEOUT_SECONDS in
     * {@code scripts/run-openbw-e2e.sh} is the same value, set once at the top of
     * that file.
     */
    public static final int TIMEOUT_SECONDS = 120;

    public static final int DEFAULT_GAME_TIMEOUT_SECONDS = TIMEOUT_SECONDS;

    /** Hard upper bound for every OpenBW simulation, independent of caller timeout. */
    public static final int MAX_GAME_TIMEOUT_SECONDS = TIMEOUT_SECONDS;

    /**
     * How long the host is given to publish its game registry before we give up.
     */
    public static final int DEFAULT_HOST_WAIT_SECONDS = 30;

    private final String harnessDir;
    private final String gameDir;
    private final String serverScript;
    private final String map;
    private final String ourRace;
    private final String enemyRace;
    private final String logDir;
    private final int gameTimeoutSeconds;
    private final int hostWaitSeconds;

    private OpenBWConfig(Builder builder) {
        this.harnessDir = builder.harnessDir;
        this.gameDir = builder.gameDir;
        this.serverScript = builder.serverScript;
        this.map = builder.map;
        this.ourRace = builder.ourRace;
        this.enemyRace = builder.enemyRace;
        this.logDir = builder.logDir;
        this.gameTimeoutSeconds = builder.gameTimeoutSeconds;
        this.hostWaitSeconds = builder.hostWaitSeconds;
    }

    /** Defaults for this machine; the common case is a map (and maybe races). */
    public static OpenBWConfig defaults() {
        return builder().build();
    }

    public static OpenBWConfig ofMap(String map) {
        return builder().map(map).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String harnessDir() {
        return harnessDir;
    }

    public String gameDir() {
        return gameDir;
    }

    public String serverScript() {
        return serverScript;
    }

    public String map() {
        return map;
    }

    public String ourRace() {
        return ourRace;
    }

    public String enemyRace() {
        return enemyRace;
    }

    public String logDir() {
        return logDir;
    }

    public int gameTimeoutSeconds() {
        return gameTimeoutSeconds;
    }

    public int hostWaitSeconds() {
        return hostWaitSeconds;
    }

    public String launcherBinary() {
        return harnessDir + "/build/bin/BWAPILauncher";
    }

    /** The files the harness needs to host a game at all. */
    public String missingRequirement() {
        if (!AFile.directoryExists(harnessDir))
            return "harness dir missing: " + harnessDir;
        if (!AFile.directoryExists(gameDir))
            return "harness game dir missing: " + gameDir;
        if (!AFile.fileExists(serverScript))
            return "host script missing: " + serverScript;
        if (!AFile.fileExists(launcherBinary())) {
            return "BWAPILauncher not built: " + launcherBinary() + " (cmake --build " + harnessDir + "/build)";
        }
        for (String mpq : new String[] { "StarDat.mpq", "BrooDat.mpq", "Patch_rt.mpq" }) {
            if (!AFile.fileExists(gameDir + "/" + mpq))
                return "missing " + mpq + " in " + gameDir;
        }
        return null;
    }

    public boolean isRunnable() {
        return missingRequirement() == null;
    }

    public static final class Builder {
        private String harnessDir = DEFAULT_HARNESS_DIR;
        private String gameDir = DEFAULT_GAME_DIR;
        private String serverScript = DEFAULT_SERVER_SCRIPT;
        private String map = DEFAULT_MAP;
        private String ourRace = DEFAULT_OUR_RACE;
        private String enemyRace = DEFAULT_ENEMY_RACE;
        private String logDir = "out/openbw";
        private int gameTimeoutSeconds = DEFAULT_GAME_TIMEOUT_SECONDS;
        private int hostWaitSeconds = DEFAULT_HOST_WAIT_SECONDS;

        public Builder harnessDir(String value) {
            this.harnessDir = value;
            return this;
        }

        public Builder gameDir(String value) {
            this.gameDir = value;
            return this;
        }

        public Builder serverScript(String value) {
            this.serverScript = value;
            return this;
        }

        public Builder map(String value) {
            this.map = value;
            return this;
        }

        public Builder races(String ours, String enemy) {
            this.ourRace = ours;
            this.enemyRace = enemy;
            return this;
        }

        public Builder logDir(String value) {
            this.logDir = value;
            return this;
        }

        public Builder gameTimeoutSeconds(int value) {
            if (value <= 0 || value > MAX_GAME_TIMEOUT_SECONDS) {
                throw new IllegalArgumentException(
                    "OpenBW simulation timeout must be between 1 and " + MAX_GAME_TIMEOUT_SECONDS + " seconds"
                );
            }
            this.gameTimeoutSeconds = value;
            return this;
        }

        public Builder hostWaitSeconds(int value) {
            this.hostWaitSeconds = value;
            return this;
        }

        public OpenBWConfig build() {
            return new OpenBWConfig(this);
        }
    }

    @Override
    public String toString() {
        return "OpenBWConfig{map=" + map + " " + ourRace + "v" + enemyRace
                + " harness=" + harnessDir + " logs=" + logDir + "}";
    }
}
