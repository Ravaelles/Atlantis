package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.debug.tweaker.ParamTweakerEvaluator;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.util.GameSummary;
import benchmark.BenchmarkMode;
import atlantis.cherryvis.ACherryVis;

public class OnGameEnd {
    private static boolean _executed = false;

    public static void execute(boolean won) {
        AGame.setWon(won);

        if (_executed) return;

        if (Env.isTesting()) {
            Atlantis.getInstance().exitGame(won);
            return;
        }

        if (!Env.isTesting()) GameSummary.print(won);
        if (Env.isParamTweaker()) ParamTweakerEvaluator.updateOnEnd(won);
        if (ACherryVis.isEnabled()) ACherryVis.finish();

//        CodeProfiler.printSummary();

        if (Env.isBenchmark()) BenchmarkMode.onGameEnd(won);

        SaveGameResultsToFile.createAndSave(won);
        if (!Env.isLocal()) A.println("Game ended at: " + A.getCurrentTimeAsString());

        _executed = true;

        if (Env.isLocal()) Atlantis.getInstance().exitGame(won);
        else Atlantis.game().leaveGame();
    }
}
