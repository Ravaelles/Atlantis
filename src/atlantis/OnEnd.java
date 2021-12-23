package atlantis;

import atlantis.env.Env;
import atlantis.tweaker.ParamTweakerEvaluator;

public class OnEnd {

    public static void execute(boolean winner) {
        if (Env.isParamTweaker()) {
            ParamTweakerEvaluator.updateOnEnd(winner);
        }
    }

}
