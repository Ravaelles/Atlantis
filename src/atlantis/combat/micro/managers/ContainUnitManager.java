package atlantis.combat.micro.managers;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.MissionUnitManager;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;

//public class ContainUnitManager extends MissionUnitManager {
//
//    private Mission mission;
//
//    public boolean updateUnit(AUnit unit) {
//        if (mission.focusPoint() == null) {
//            unit.setTooltip("#NoContainPoint");
//            return false;
//        }
//
//        return handleComeCloserToChokepoint(unit);
//    }
//
//    // =========================================================
//
//    private boolean handleComeCloserToChokepoint(AUnit unit) {
//        if (unit.distTo(mission.focusPoint()) > optimalDistance()) {
//            return unit.move(
//                    mission.focusPoint(),
//                    UnitActions.MOVE_TO_FOCUS,
//                    "#Contain(" + A.digit(mission.focusPoint().distTo(unit)) + ")"
//            );
//        }
//
//        return false;
//    }
//
//    private double optimalDistance() {
//        return 6.1;
//    }
//
//}