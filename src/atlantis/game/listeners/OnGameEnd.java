package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.debug.tweaker.ParamTweakerEvaluator;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.util.GameSummary;
import atlantis.util.AConsole;
import benchmark.BenchmarkMode;
import atlantis.cherryvis.ACherryVis;

import java.util.Timer;
import java.util.TimerTask;

public class OnGameEnd {
    /**
     * Guards the end-of-game cleanup so it runs once per game, not once per
     * {@code onEnd} callback (the engine may deliver more than one). It must be
     * <b>reset for every game</b>: as a plain static it leaked between games in
     * one JVM - the same class of leak {@code UnitsArchive.reset()} and
     * {@code ReservedResources.reset()} were fixed for - and a first spurious
     * {@code onEnd} permanently disarmed every later one.
     */
    private static boolean _executed = false;

    /** Called from the per-game initialisation so the latch starts fresh. */
    public static void reset() {
        _executed = false;
    }

    /**
     * Shutdown-hook entry (SIGTERM, Escape-kill, wall timeout): the game is
     * over but {@code onEnd} never arrived, so run the end cleanup with a
     * defeat. The latch makes this a no-op after a clean end, and a missing
     * game (supervisor JVM exiting) returns before touching anything.
     */
    public static void onShutdownHook() {
        try {
            if (_executed) return;
            if (atlantis.Atlantis.game() == null) return;
            execute(false);
        } catch (Throwable ignored) {
        }
    }

    /**
     * True once the end-of-game cleanup for the current game has run. Exposed so
     * the reset/symmetry contract can be asserted without entering the
     * {@code System.exit} exit path (NEXT #49).
     */
    public static boolean hasExecuted() {
        return _executed;
    }

    /**
     * Marks the cleanup as done without running it. The exit path cannot be
     * exercised in a JVM test (it calls {@code System.exit}), so the latch rule
     * is pinned through this seam: a spurious first {@code onEnd} must disarm
     * later ones. Deliberately package-private in spirit (public for tests in
     * another package to reach it), and only ever called from tests.
     */
    public static void markExecutedForTest() {
        _executed = true;
    }

    public static void execute(boolean won) {
        AGame.setWon(won);

        if (_executed) return;

        if (Env.isTesting()) {
            // Mark done BEFORE returning: this branch used to return without
            // setting the latch, so the very first call left it false and every
            // later onEnd ran the full cleanup path on a game that had already
            // been torn down. Setting it here makes the guard symmetric.
            _executed = true;
            Atlantis.getInstance().exitGame(won);
            return;
        }

        // Diagnostics first: on a dying game (Escape-kill, dead host) the
        // summary below may throw midway, and the ledger tail must land
        // regardless. Both writes are delimited, stream order was never
        // contractual across stdout/stderr.
        atlantis.util.log.EventLog.flush();
        atlantis.game.util.BuildingLedger.printTailToStderr();

        if (!Env.isTesting()) GameSummary.print(won);
        if (Env.isParamTweaker()) ParamTweakerEvaluator.updateOnEnd(won);
        if (ACherryVis.isEnabled()) ACherryVis.finish();

//        CodeProfiler.printSummary();

        if (Env.isBenchmark()) BenchmarkMode.onGameEnd(won);

        SaveGameResultsToFile.createAndSave(won);
        if (!Env.isLocal()) AConsole.println("Game ended at: " + A.getCurrentTimeAsString());

        _executed = true;

        // Backstop: the exit path below is millisecond-scale IO (summary print,
        // trace write, two kills). A run that reaches here must be gone seconds
        // later - if it is not, something in the path is hung and the run would
        // otherwise burn the whole bot-cap (measured 101 s of silence after a
        // 9 s game). The timer fires only then: 15 s keeps the worst case under
        // the owner's 60 s ask (43 s game + 15). halt(), not exit(): a hang can
        // sit inside a shutdown hook, which exit() would run again.
        // Skipped for END_GAME_VIA_ENGINE (the bot is supposed to linger there)
        // and in tests (a halted test JVM helps nobody).
        if (!Env.endGameViaEngine()) {
            Timer backstop = new Timer("onend-backstop", true);
            backstop.schedule(new TimerTask() {
                @Override
                public void run() {
                    AConsole.errPrintln(
                        "OnGameEnd backstop: exit path did not finish in 15s - forcing halt"
                    );
                    Runtime.getRuntime().halt(1);
                }
            }, 15000);
        }

        // END_GAME_VIA_ENGINE lets the engine end the match instead of this JVM killing
        // itself, which is what lets the engine flush its replay (it writes the file in
        // onGameEnd(), reached only after the launcher's game loop exits). Off by
        // default - the self-exit is the bounded-run shape CONVENTIONS §17 relies on.
        if (Env.endGameViaEngine()) Atlantis.game().leaveGame();
        else if (Env.isLocal()) Atlantis.getInstance().exitGame(won);
        else Atlantis.game().leaveGame();
    }
}
