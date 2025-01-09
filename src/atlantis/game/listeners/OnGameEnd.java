package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.debug.tweaker.ParamTweakerEvaluator;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.Enemy;
import atlantis.game.util.GameSummary;
import atlantis.map.AMap;
import atlantis.util.WriteJsonToFile;
import benchmark.BenchmarkMode;
import cherryvis.ACherryVis;

public class OnGameEnd {
    private static boolean _executed = false;

    public static void execute(boolean winner) {
        if (_executed) return;

        if (Env.isTesting()) {
            Atlantis.getInstance().exitGame(winner);
            return;
        }

        if (!Env.isTesting()) GameSummary.print(winner);

        if (Env.isParamTweaker()) ParamTweakerEvaluator.updateOnEnd(winner);


        if (ACherryVis.isEnabled()) ACherryVis.finish();

//        CodeProfiler.printSummary();

        if (Env.isBenchmark()) BenchmarkMode.onGameEnd(winner);

        saveResultToFile(winner);
        if (!Env.isLocal()) A.println("Game ended at: " + A.getCurrentTimeAsString());

        _executed = true;

        if (Env.isLocal()) Atlantis.getInstance().exitGame(winner);
        else Atlantis.game().leaveGame();
    }

    private static void saveResultToFile(boolean winner) {
        String[] headers = {"Date", "Result", "Enemy", "Map", "Time [s]", "Killed/Lost", "Resource balance"};

        String formatting = "%-18s %-8s %-16s %-32s %-9s %-14s %-16s";

        String[] values = {
            A.getCurrentDateInFormatYMDHHmm(),
            winner ? "WIN" : "LOSS",
            Enemy.name(),
            AMap.mapFileNameWithoutPath(),
            AGame.timeSeconds() + "",
            "K:" + Atlantis.KILLED + "/ L:" + Atlantis.LOST,
            A.resourcesBalance() + ""
        };
        String filePath = "bwapi-data/write/game_results.txt";

//        boolean result = A.appendToFile(filePath, content);
        boolean result = WriteJsonToFile.writeOrAppend(
            filePath, headers, values, 32, true
        );

        A.println("@@@@@@@@@@@@@@@@@@@@ Game result saved to: " + filePath
            + " / result:" + (result ? "OK" : "FAILED")
            + " / exists:" + A.fileExists(filePath)
            + " / current working directory:" + System.getProperty("user.dir")
        );
    }
}
