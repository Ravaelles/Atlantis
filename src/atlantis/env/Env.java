package atlantis.env;

import atlantis.util.A;

/**
 * Aim of Env is to differentiate between LOCAL, TESTING and PRODUCTION (any online tournaments).
 * We don't want to print out too much data in production.
 */
public class Env {

    private static final String FILE = "ENV";

    private static boolean isLocal = false;
    private static boolean firstRun = true;
    private static boolean paramTweaker = false;

    // =========================================================

    public static void readEnvFile(String[] mainArgs) {
        String[][] env = A.loadFile(FILE, 2, "=");

        for (String[] line : env) {
            String key = line[0].toLowerCase();
            String value = line[1];

            switch (key) {
                case "local": isLocal = trueFalse(value);
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
}
