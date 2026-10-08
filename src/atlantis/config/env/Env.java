package atlantis.config.env;

import atlantis.config.AtlantisIgniter;
import atlantis.game.AGame;
import atlantis.information.decisions.ForceExitLocallyAfterRealSeconds;
import atlantis.information.decisions.GGForEnemy;
import atlantis.util.AFile;
import atlantis.util.log.ErrorLog;
import benchmark.BenchmarkMode;

import java.io.File;

/**
 * Aim of Env is to differentiate between LOCAL, TESTING and PRODUCTION (any online tournaments).
 * We don't want to print out too much data in production.
 */
public class Env {
    private static boolean firstRun = true;
    private static boolean generateCherryVisReplay = false;
    private static boolean isBenchmark = false;
    private static boolean isLocal = false;
    private static boolean isTesting = false;
    private static boolean modifyBwapiIni = false;
    private static boolean openBW = false;
    private static boolean wine = false;
    private static boolean wineClient = false;
    private static boolean paramTweaker = false;
    private static String copyCherryVisDataTo = null;
    /**
     * production-v2 mode (M4 of _AI/redesign/01_PRODUCTION.md): DRY_RUN runs the
     * new engine in parallel with the legacy queue and only logs what it would
     * order; LIVE lets it issue the orders. {@code PRODUCTION_V2=false} (or a
     * missing key) keeps the legacy engine alone - the default until a dry-run
     * game has been read.
     */
    private static ProductionV2Mode productionV2 = ProductionV2Mode.OFF;

    // =========================================================

    public static void readEnvFile(String[] mainArgs) {
        isBenchmark = BenchmarkMode.detectBenchmarkMode(mainArgs);

        if (!AFile.fileExists(envFilePath())) {
            AGame.exit(
                "ENV file doesn't exist (" + (new File(envFilePath())).getAbsolutePath()
                    + ")\nPlease create it by copying ENV-EXAMPLE file and renaming it."
            );
        }

        String[][] env = AFile.loadFile(envFilePath(), 2, "=");

        for (String[] line : env) {
            convertEnvLineIntoFlag(line);
        }

        if (mainArgsContains("--param-tweaker", mainArgs)) {
            paramTweaker = true;
        }
        if (mainArgsContains("--counter=", mainArgs) && !mainArgsEquals("--counter=1", mainArgs)) {
            firstRun = false;
        }
    }

    private static void convertEnvLineIntoFlag(String[] line) {
        String key = line[0].toUpperCase();
        if (key.length() > 0 && key.charAt(0) == '#') {
            return;
        }
        if (key.trim().length() == 0) {
            return;
        }
        if (line.length < 2) {
            return;
        }
        // AFile.loadFile keeps the separator as the last character of the value
        // (`BWAPI_DATA_PATH=C:\x\` arrives as "C:\x\"" + "=""" - measured
        // 2026-10-08, it produced a path ending in "/null"). Strip it.
        String value = line[1];
        if (value != null && value.endsWith("=")) {
            value = value.substring(0, value.length() - 1);
        }

        applyKeyAndValueToFlag(key, value);
    }

    private static boolean applyKeyAndValueToFlag(String key, String value) {
        switch (key) {
            case "BWAPI_DATA_PATH":
                AtlantisIgniter.setBwapiDataPath(value);
                return true;
            case "CHAOS_LAUNCHER_PATH":
                AtlantisIgniter.setChaosLauncherPath(value);
                return true;
            case "FORCE_GG_FOR_ENEMY":
                GGForEnemy.allowed = "true".equals(value);
                return true;
            case "FORCE_END_GAME_AFTER_REAL_SECONDS":
                ForceExitLocallyAfterRealSeconds.realSecondsLimit = toInt(value);
                return true;
            case "LOCAL":
                isLocal = trueFalse(value);
                return true;
            case "GENERATE_CHERRYVIS_REPLAY":
                generateCherryVisReplay = trueFalse(value);
                return true;
            case "MODIFY_BWAPI_INI":
                modifyBwapiIni = trueFalse(value);
                return true;
            case "POSTGAME_COPY_CHERRYVIS_TO":
                copyCherryVisDataTo = value;
                return true;
            case "PRODUCTION_V2":
                productionV2 = ProductionV2Mode.parse(value);
                return true;
            case "GAME_LAUNCHER":
                // CHAOS (default, Windows + ChaosLauncher), OPENBW (Linux +
                // BWAPILauncher server) or WINE (Linux + real StarCraft under
                // Wine with ChaosLauncher).
                String launcher = value == null ? "" : value.trim();
                openBW = launcher.equalsIgnoreCase("OPENBW");
                wine = launcher.equalsIgnoreCase("WINE");
                wineClient = launcher.equalsIgnoreCase("WINECLIENT");
                return true;
            default:
                // Wine game-window geometry, tweakable without recompiling.
                atlantis.config.launcher.WineWindowConfig.applyEnvValue(key, value);
                return false;
        }
    }

    private static String envFilePath() {
//        if (AFile.currentPath().contains("D:\\")) {
        if (AFile.fileExists("bwapi-data/AI/ENV")) return "bwapi-data/AI/ENV";
        if (AFile.fileExists("../bwapi-data/AI/ENV")) return "../bwapi-data/AI/ENV";

        return "ENV";
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

    private static int toInt(String value) {
        if (value == null || value.isEmpty()) {
            return -1;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            ErrorLog.printErrorOnce("Error parsing ENV file. Value=" + value + " / " + e.getMessage() + "\n");
            return -1;
        }
    }

    /**
     * Should be false for tournaments, true for local development.
     */
    public static boolean isLocal() {
        return isLocal;
    }

    public static boolean isTournament() {
        return !isLocal;
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
        return isTesting;
    }

    /**
     * production-v2 mode from {@code PRODUCTION_V2} in ENV: OFF (default,
     * legacy engine only), DRY_RUN (v2 plans in parallel and only logs), or
     * LIVE (v2 issues the commands).
     */
    public static ProductionV2Mode productionV2() {
        return productionV2;
    }

    public static boolean isBenchmark() {
        return isBenchmark;
    }

    /**
     * OpenBW backend (Linux, headless server) selected via
     * {@code GAME_LAUNCHER=OPENBW} in {@code bwapi-data/AI/ENV}.
     * Anything else (including a missing key) means the classic
     * Windows + ChaosLauncher backend, so old setups keep working.
     */
    public static boolean isOpenBW() {
        return openBW;
    }

    /**
     * Wine backend (Linux, real StarCraft + ChaosLauncher) selected via
     * {@code GAME_LAUNCHER=WINE} in {@code bwapi-data/AI/ENV}.
     */
    public static boolean isWine() {
        return wine;
    }

    /**
     * Wine-client backend ({@code GAME_LAUNCHER=WINECLIENT}): the JVM runs
     * under Wine and only attaches to an already-running StarCraft - it must
     * not start or kill anything.
     */
    public static boolean isWineClient() {
        return wineClient;
    }

    public static void markIsTesting(boolean enabled) {
        isTesting = enabled;
    }

    public static String copyCherryVisDataTo() {
        return copyCherryVisDataTo;
    }

    public static boolean shouldModifyBwapiIni() {
        return modifyBwapiIni;
    }

    public static boolean generateCherryVisReplay() {
        return generateCherryVisReplay;
    }
}
