package atlantis.game.events;

import atlantis.config.env.Env;
import atlantis.debug.profiler.CodeProfiler;
import atlantis.debug.tweaker.ParamTweakerEvaluator;

public class OnGameEnd {
    public static void execute(boolean winner) {
        if (Env.isParamTweaker()) {
            ParamTweakerEvaluator.updateOnEnd(winner);
        }

//        CodeProfiler.printSummary();
    }
}
