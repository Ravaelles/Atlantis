package atlantis.debug.tweaker;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.game.A;

public class ParamTweakerFactory extends ParamTweaker {

    public static void init() {
        A.println("###### INIT ParamTweaker ######");
        tweaker = new ParamTweaker();

//        tweaker.addParam(new Param(
//                "ENEMIES_Near_FACTOR",
//                () -> SafetyMarginAgainstMelee.ENEMIES_Near_FACTOR,
//                () -> SafetyMarginAgainstMelee.ENEMIES_Near_FACTOR = A.rand(0, 200) / 100.0
//        ));
//        tweaker.addParam(new Param(
//                "ENEMIES_Near_MAX_DIST",
//                () -> SafetyMarginAgainstMelee.ENEMIES_Near_MAX_DIST,
//                () -> SafetyMarginAgainstMelee.ENEMIES_Near_MAX_DIST = A.rand(120, 260) / 100.0
//        ));
        tweaker.addParam(new Param(
                "SafetyMeleeInfantryBaseNoMedic",
                () -> SafetyMarginAgainstMelee.INFANTRY_BASE_IF_NO_MEDIC,
                () -> SafetyMarginAgainstMelee.INFANTRY_BASE_IF_NO_MEDIC = A.rand(220, 250) / 100.0
        ));
        tweaker.addParam(new Param(
                "SafetyMeleeInfantryWoundNoMedic",
                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC,
                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC = A.rand(70, 85)
        ));
//        tweaker.addParam(new Param(
//                "SafetyMeleeInfantryBaseIfMedic",
//                () -> SafetyMarginAgainstMelee.INFANTRY_BASE_IF_MEDIC,
//                () -> SafetyMarginAgainstMelee.INFANTRY_BASE_IF_MEDIC = A.rand(60, 164) / 100.0
////                () -> SafetyMarginAgainstMelee.INFANTRY_BASE_IF_MEDIC = A.rand(175, 255) / 100.0
//        ));
//        tweaker.addParam(new Param(
//                "SafetyMeleeInfantryWoundIfMedic",
//                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND_IF_MEDIC,
//                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND_IF_MEDIC = A.rand(19, 23)
////                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND_IF_MEDIC = A.rand(30, 99)
//        ));
//        tweaker.addParam(new Param(
//                "RunAnyDirectionRadiusInfantry",
//                () -> ARunningManager.ANY_DIRECTION_INIT_RADIUS_INFANTRY,
//                () -> ARunningManager.ANY_DIRECTION_INIT_RADIUS_INFANTRY = A.rand(10, 60) / 10
//        ));
//        tweaker.addParam(new Param(
//                "RunNotifyUnitsInRadiusBase",
//                () -> ARunningManager.NOTIFY_UNITS_IN_RADIUS_BASE,
//                () -> ARunningManager.NOTIFY_UNITS_IN_RADIUS_BASE = A.rand(70, 95) / 100.0
//        ));
//        tweaker.addParam(new Param(
//                "NearUnitMakeSpace",
//                () -> ARunningManager.Near_UNIT_MAKE_SPACE,
//                () -> ARunningManager.Near_UNIT_MAKE_SPACE = A.rand(60, 90) / 100.0
//        ));
//        tweaker.addParam(new Param(
//                "STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO",
//                () -> ARunningManager.STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO,
//                () -> ARunningManager.STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO = A.rand(0, 10)
//        ));
//        tweaker.addParam(new Param(
//                "MIN_DIST_TO_ASSIGNMENT",
//                () -> TerranMedic.MIN_DIST_TO_ASSIGNMENT,
//                () -> TerranMedic.MIN_DIST_TO_ASSIGNMENT = A.rand(50, 200) / 100.0
//        ));

        tweaker.initParamValues();
    }

}
