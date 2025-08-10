package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.debug.tweaker.ParamTweakerEvaluator;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.Enemy;
import atlantis.map.AMap;
import benchmark.BenchmarkMode;
import cherryvis.ACherryVis;

public class OnGameEnd {
    public static void execute(boolean winner) {
        if (Env.isParamTweaker()) ParamTweakerEvaluator.updateOnEnd(winner);

        if (!Env.isLocal()) A.println("Game ended at: " + A.getCurrentTimeAsString());

        if (ACherryVis.isEnabled()) ACherryVis.finish();

//        CodeProfiler.printSummary();

        if (Env.isBenchmark()) BenchmarkMode.onGameEnd(winner);

        saveResultToFile(winner);
    }

    private static void saveResultToFile(boolean winner) {
        String formatting = "%-18s %-8s %-16s %-32s %-9s %-12s %-14s";

        String content = String.format(formatting,
            A.getCurrentDateInFormatYMDHHmm(),
            winner ? "WIN" : "LOSE",
            Enemy.name(),
            AMap.mapFileNameWithoutPath(),
            AGame.timeSeconds(),
            "K:" + Atlantis.KILLED + "/ L:" + Atlantis.LOST,
            A.resourcesBalance()
        );
        String filePath = "game_results.txt";

        if (!A.fileExists(filePath)) {
            content = String.format(
                formatting + "%n",
                "Date", "Result", "Enemy", "Map", "Time [s]", "Killed/Lost", "Resource balance"
            ) + content;
        }
    
        A.writeToFile(filePath, content);
    }
}
