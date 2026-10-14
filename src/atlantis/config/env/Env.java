package atlantis.config.env;

import atlantis.config.AtlantisIgniter;
import atlantis.game.AGame;
import atlantis.information.decisions.ForceExitLocallyAfterSomeTime;
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
    /**
     * production-v2 mode (M4 of _AI/redesign/01_PRODUCTION.md): DRY_RUN runs the
     * new engine in parallel with the legacy queue and only logs what it would
     * order; LIVE lets it issue the orders. {@code PRODUCTION_V2=false} (or a
     * missing key) keeps the legacy engine alone - the default until a dry-run
     * game has been read.
     */
    private static ProductionV2Mode productionV2 = ProductionV2Mode.OFF;

    /**
     * PLACEMENT=CATALOGUE selects the rewritten planner; false keeps legacy.
     */
    private static boolean placementCatalogue = false;

    /**
     * BUILDER_LOG=1 prints, every frame, each builder's active manager, its last
     * issued command and its target position.
     *
     * <p>
     * A debugging switch, off by default, for the "the construction is assigned and
     * buildable but never starts" class of bug: those failures are invisible in a
     * normal log because nothing throws - the builder simply keeps being handled by a
     * manager that never issues the order. The active manager and the last command are
     * the two facts that separate "no order is issued" from "an order is issued to the
     * wrong place", and neither is in the log today. See {@code BuilderDiagnostics}.
     * </p>
     */
    private static boolean builderLog = false;

    /**
     * CORE_AND_DRAGOON_GATE=true ends the game as soon as the opening is proven.
     *
     * <p>
     * The {@code --short} tier of {@code run-openbw-e2e.sh}: a finished Cybernetics Core and
     * at least one Dragoon. Waiting the full game length to learn that the opening never got
     * there is the expensive way to ask the question, and a production change is usually
     * checked with exactly this question. The shell asserts the same two facts at the end, so
     * this only shortens a run that would have passed anyway - it cannot turn a failure green.
     * </p>
     */
    private static boolean coreAndDragoonGate = false;

    /**
     * END_GAME_VIA_ENGINE=true makes the bot ask the engine to leave the game instead of
     * killing its own JVM at game end.
     *
     * <p>
     * Why it exists: a replay is written by the engine in {@code onGameEnd()}, which the
     * launcher calls only after its game loop exits - and a bot that {@code System.exit}s
     * (the {@code LOCAL=true} path) is killed from under it first, so a bounded run
     * produces no {@code .rep} (measured 2026-10-10, {@code _AI/PLAN-OPENBW.md}). Letting
     * the engine end the game is what makes the file appear.
     * </p>
     *
     * <p>
     * <b>Off by default, deliberately.</b> The self-exit is the shape CONVENTIONS §17
     * relies on for a bounded run (the bot ends its own game, the host outlives it and is
     * torn down cleanly), so this is a diagnostic mode for recording, not the new normal.
     * </p>
     */
    private static boolean endGameViaEngine = false;

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
                // Cap enforced again here, so a hand-edited ENV cannot widen the
                // wall-clock limit past the OpenBW simulation cap (CONVENTIONS §17).
                ForceExitLocallyAfterSomeTime.realSecondsLimit = Math.min(toInt(value), 120);
                return true;
            case "FORCE_END_GAME_AFTER_INGAME_SECONDS":
                ForceExitLocallyAfterSomeTime.inGameSecondsLimit = toInt(value);
                return true;
            case "LOCAL":
                isLocal = trueFalse(value);
                return true;
            case "END_GAME_VIA_ENGINE":
                endGameViaEngine = trueFalse(value);
                return true;
            case "GENERATE_CHERRYVIS_REPLAY":
                generateCherryVisReplay = trueFalse(value);
                return true;
            case "MODIFY_BWAPI_INI":
                modifyBwapiIni = trueFalse(value);
                return true;
            case "PRODUCTION_V2":
                productionV2 = ProductionV2Mode.parse(value);
                return true;
            case "BUILDER_LOG":
                builderLog = "1".equals(value == null ? "" : value.trim())
                    || "TRUE".equalsIgnoreCase(value == null ? "" : value.trim());
                return true;
            case "CORE_AND_DRAGOON_GATE":
                // --short tier of the OpenBW E2E: end the game as soon as the opening is
                // PROVEN (a finished Cybernetics Core and at least one Dragoon), instead of
                // playing the full game length. The shell asserts the same facts at the end,
                // so a run that exits early without them is still a failure.
                coreAndDragoonGate = trueFalse(value);
                return true;
            case "PLACEMENT":
                // Which placement implementation answers the scheduler's seam:
                // CATALOGUE selects the rewritten planner (_AI/redesign/03_PLACEMENT.md),
                // anything else (or absent) keeps the legacy APositionFinder.
                // Read here rather than from the process environment: ENV is a FILE
                // parsed by this class, so System.getenv never sees it (measured
                // 2026-10-08 - the flag was written to ENV and silently ignored).
                placementCatalogue = "CATALOGUE".equalsIgnoreCase(value == null ? "" : value.trim());
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

    /**
     * Which ENV file the bot actually plays with (or {@code ENV} fallback
     * when none exists - then every flag silently runs its default, which is
     * how a Wine game once played a whole match with builder logging off).
     */
    public static String envSource() {
        return envFilePath();
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

    /**
     * True when {@code PLACEMENT=CATALOGUE} selected the rewritten placement
     * planner. False (the default) keeps the legacy {@code APositionFinder}, so a
     * build with no flag behaves exactly as before.
     */
    public static boolean placementCatalogue() {
        return placementCatalogue;
    }

    /**
     * True when {@code BUILDER_LOG=1} is set in ENV: print each builder's active
     * manager, last command and target every frame. Off by default; it is a
     * diagnostic switch, not a feature.
     */
    public static boolean builderLog() {
        return builderLog;
    }

    /**
     * True when {@code CORE_AND_DRAGOON_GATE=true}: end the game as soon as the opening is
     * proven (a finished Cybernetics Core and at least one Dragoon). The {@code --short} tier
     * of the OpenBW E2E runner; off by default.
     */
    public static boolean coreAndDragoonGate() {
        return coreAndDragoonGate;
    }

    /**
     * True when {@code END_GAME_VIA_ENGINE=true}: end the match through the engine
     * ({@code leaveGame()}) rather than exiting the JVM, so the engine gets to flush its
     * replay. See the flag's own javadoc for why this is off by default.
     */
    public static boolean endGameViaEngine() {
        return endGameViaEngine;
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

    /** Test seam (the --short gate is ENV-driven and has no other writer). */
    public static void setCoreAndDragoonGateForTest(boolean enabled) {
        coreAndDragoonGate = enabled;
    }

    public static boolean shouldModifyBwapiIni() {
        return modifyBwapiIni;
    }

    public static boolean generateCherryVisReplay() {
        return generateCherryVisReplay;
    }
}
