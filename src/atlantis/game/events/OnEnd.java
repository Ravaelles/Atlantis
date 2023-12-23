package atlantis.game.events;

import atlantis.config.env.Env;
import atlantis.debug.tweaker.ParamTweakerEvaluator;

public class OnEnd {

    public static void execute(boolean winner) {
        if (Env.isParamTweaker()) {
            ParamTweakerEvaluator.updateOnEnd(winner);
        }
    }

}
