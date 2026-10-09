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

        if (!Env.isTesting()) GameSummary.print(won);
        if (Env.isParamTweaker()) ParamTweakerEvaluator.updateOnEnd(won);
        if (ACherryVis.isEnabled()) ACherryVis.finish();

//        CodeProfiler.printSummary();

        if (Env.isBenchmark()) BenchmarkMode.onGameEnd(won);

        SaveGameResultsToFile.createAndSave(won);
        if (!Env.isLocal()) AConsole.println("Game ended at: " + A.getCurrentTimeAsString());

        _executed = true;

        if (Env.isLocal()) Atlantis.getInstance().exitGame(won);
        else Atlantis.game().leaveGame();
    }
}
