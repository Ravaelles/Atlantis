package atlantis.env;

import atlantis.util.A;

public class Env {

    private static final String FILE = "ENV";

    private static boolean isLocal = false;
    private static boolean paramTweaker = false;

    // =========================================================

    public static void readEnvFile(String[] mainArgs) {
        String[][] env = A.loadFile(FILE, 2, "=");

        for (String[] line : env) {
            String key = line[0].toLowerCase();
            String value = line[1];
//            System.out.println(key + " // " + value);

            switch (key) {
                case "local": isLocal = trueFalse(value);
            }
        }
        
        if (mainArgsContains("--param-tweaker", mainArgs)) {
            paramTweaker = true;
        }
    }

    // =========================================================

    private static boolean mainArgsContains(String value, String[] mainArgs) {
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
}
