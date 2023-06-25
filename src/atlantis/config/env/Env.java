package atlantis.config.env;

import atlantis.config.AtlantisIgniter;
import atlantis.game.A;
import atlantis.game.AGame;

/**
 * Aim of Env is to differentiate between LOCAL, TESTING and PRODUCTION (any online tournaments).
 * We don't want to print out too much data in production.
 */
public class Env {

    private static final String ENV_FILE_PATH = "ENV";

    private static boolean isLocal = false;
    private static boolean firstRun = true;
    private static boolean paramTweaker = false;

    // =========================================================

    public static void readEnvFile(String[] mainArgs) {
        if (!A.fileExists(ENV_FILE_PATH)) {
            AGame.exit("ENV file doesn't exist. Please create it by copying ENV-EXAMPLE file and renaming it.");
        }

        String[][] env = A.loadFile(ENV_FILE_PATH, 2, "=");

        for (String[] line : env) {
            String key = line[0].toUpperCase();
            if (key.length() > 0 && key.charAt(0) == '#') {
                continue;
            }
            if (key.trim().length() == 0) {
                continue;
            }
            String value = line[1];

            switch (key) {
                case "LOCAL": isLocal = trueFalse(value);
                case "BWAPI_DATA_PATH": AtlantisIgniter.setBwapiDataPath(value);
                case "CHAOS_LAUNCHER_PATH": AtlantisIgniter.setChaosLauncherPath(value);
            }
        }

        if (mainArgsContains("--param-tweaker", mainArgs)) {
            paramTweaker = true;
        }
        if (mainArgsContains("--counter=", mainArgs) && !mainArgsEquals("--counter=1", mainArgs)) {
            firstRun = false;
        }
    }

    // =========================================================

    private static boolean mainArgsContains(String value, String[] mainArgs) {
        for (String arg : mainArgs) {
            if (arg != null && arg.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean mainArgsEquals(String value, String[] mainArgs) {
        for (String arg : mainArgs) {
            if (arg != null && arg.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean trueFalse(String value) {
        return value != null && value.equals("true");
    }

    /**
     * Should be false for tournaments, true for local development.
     */
    public static boolean isLocal() {
        return isLocal;
    }

    /**
     * Special "Param tweaker" mode, game should be run as quickly as possible.
     */
    public static boolean isParamTweaker() {
        return paramTweaker;
    }

    public static boolean isFirstRun() {
        return firstRun;
    }

    public static boolean isTesting() {
        return false;
    }
}
