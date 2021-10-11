package atlantis.combat.micro.managers;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.MissionUnitManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public class AdvanceUnitsManager extends MissionUnitManager {

    private Mission mission;

    public boolean updateUnit(AUnit unit) {
        unit.setTooltip("#Adv");

        if (unit.distanceTo(mission.focusPoint()) > 6) {
            unit.move(mission.focusPoint(), UnitActions.MOVE_TO_ENGAGE, "#MA:Forward!");
            return true;
        }

        return false;
    }

    public static boolean moveToFocusPoint(AUnit unit, APosition focusPoint) {
//        Select<AUnit> nearbyAllies = Select.ourCombatUnits().inRadius(10, unit);
//        if (nearbyAllies.count() <= 4) {
//            unit.move(nearbyAllies.first().getPosition(), UnitActions.TOGETHER);
//            unit.setTooltip("#MA:Concentrate!");
//            return true;
//        }

        double distToFocusPoint = unit.distanceTo(focusPoint);

        if (distToFocusPoint >= 10) {
            unit.move(focusPoint, UnitActions.MOVE_TO_ENGAGE, "#Adv");
            return true;
        }
        else if (distToFocusPoint <= 3 && unit.moveAwayFrom(focusPoint, 2.5, "#Adv:Too close!")) {
            unit.setTooltip("#Adv:Too close!");
            return true;
        }
        else if (distToFocusPoint <= 6) {
            if (unit.getOrderTarget() != null) {
                unit.holdPosition("#Adv:Good");
            }
            return true;
        }

        return false;
    }

}