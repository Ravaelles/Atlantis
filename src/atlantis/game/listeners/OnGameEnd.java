package atlantis.game.listeners;

import atlantis.config.env.Env;
import atlantis.debug.tweaker.ParamTweakerEvaluator;
import atlantis.game.A;

public class OnGameEnd {
    public static void execute(boolean winner) {
        if (Env.isParamTweaker()) {
            ParamTweakerEvaluator.updateOnEnd(winner);
        }

        if (!Env.isLocal()) A.println("Game ended at: " + A.getCurrentTimeAsString());

//        CodeProfiler.printSummary();
    }
}
