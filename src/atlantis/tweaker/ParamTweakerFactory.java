package atlantis.tweaker;

import atlantis.combat.micro.avoid.SafetyMarginAgainstMelee;
import atlantis.combat.retreating.ARunningManager;
import atlantis.util.A;

public class ParamTweakerFactory extends ParamTweaker {

    public static void init() {
        System.out.println("###### INIT ParamTweaker ######");

        tweaker = new ParamTweaker();

        tweaker.addParam(new Param(
                "SafetyMeleeInfantryBase",
                () -> SafetyMarginAgainstMelee.INFANTRY_BASE,
                () -> SafetyMarginAgainstMelee.INFANTRY_BASE = A.rand(46, 87) / 100.0
        ));
        tweaker.addParam(new Param(
                "SafetyMeleeInfantryWound",
                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND,
                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND = A.rand(13, 30)
        ));
//        tweaker.addParam(new Param(
//                "RunAnyDirectionRadiusInfantry",
//                () -> ARunningManager.ANY_DIRECTION_INIT_RADIUS_INFANTRY,
//                () -> ARunningManager.ANY_DIRECTION_INIT_RADIUS_INFANTRY = A.rand(20, 60) / 10
//        ));
//        tweaker.addParam(new Param(
//                "RunNotifyUnitsInRadiusBase",
//                () -> ARunningManager.NOTIFY_UNITS_IN_RADIUS_BASE,
//                () -> ARunningManager.NOTIFY_UNITS_IN_RADIUS_BASE = A.rand(0, 70) / 100.0
//        ));

        tweaker.initParamValues();
    }

}
