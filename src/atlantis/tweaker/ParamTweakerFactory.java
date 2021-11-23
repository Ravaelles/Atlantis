package atlantis.tweaker;

import atlantis.combat.micro.avoid.SafetyMarginAgainstMelee;
import atlantis.combat.micro.terran.TerranMedic;
import atlantis.combat.retreating.ARunningManager;
import atlantis.util.A;

public class ParamTweakerFactory extends ParamTweaker {

    public static void init() {
        System.out.println("###### INIT ParamTweaker ######");
        tweaker = new ParamTweaker();

//        tweaker.addParam(new Param(
//                "SafetyMeleeInfantryBase",
//                () -> SafetyMarginAgainstMelee.INFANTRY_BASE,
//                () -> SafetyMarginAgainstMelee.INFANTRY_BASE = A.rand(60, 64) / 100.0
//        ));
//        tweaker.addParam(new Param(
//                "SafetyMeleeInfantryWound",
//                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND,
//                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND = A.rand(19, 21)
//        ));
//        tweaker.addParam(new Param(
//                "RunAnyDirectionRadiusInfantry",
//                () -> ARunningManager.ANY_DIRECTION_INIT_RADIUS_INFANTRY,
//                () -> ARunningManager.ANY_DIRECTION_INIT_RADIUS_INFANTRY = A.rand(20, 60) / 10
//        ));
//        tweaker.addParam(new Param(
//                "RunNotifyUnitsInRadiusBase",
//                () -> ARunningManager.NOTIFY_UNITS_IN_RADIUS_BASE,
//                () -> ARunningManager.NOTIFY_UNITS_IN_RADIUS_BASE = A.rand(70, 95) / 100.0
//        ));
//        tweaker.addParam(new Param(
//                "NearbyUnitMakeSpace",
//                () -> ARunningManager.NEARBY_UNIT_MAKE_SPACE,
//                () -> ARunningManager.NEARBY_UNIT_MAKE_SPACE = A.rand(60, 90) / 100.0
//        ));
//        tweaker.addParam(new Param(
//                "STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO",
//                () -> ARunningManager.STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO,
//                () -> ARunningManager.STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO = A.rand(0, 10)
//        ));
//        tweaker.addParam(new Param(
//                "MIN_DIST_TO_ASSIGNMENT",
//                () -> TerranMedic.MIN_DIST_TO_ASSIGNMENT,
//                () -> TerranMedic.MIN_DIST_TO_ASSIGNMENT = A.rand(50, 200) / 100.0
//        ));

        tweaker.initParamValues();
    }

}
