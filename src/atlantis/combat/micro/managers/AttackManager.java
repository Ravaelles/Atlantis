package atlantis.combat.micro.managers;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.MissionUnitManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public class AttackManager extends MissionUnitManager {

    private Mission mission;

    public boolean updateUnit(AUnit unit) {
        unit.setTooltip("#MAttack");

        if (unit.distanceTo(mission.focusPoint()) > 6) {
//            unit.move(mission.focusPoint(), UnitActions.MOVE);
            unit.attackPosition(mission.focusPoint());
            unit.setTooltip("#MA:Forward!");
            return true;
        }

        return false;
    }

    public static boolean attackFocusPoint(AUnit unit, APosition focusPoint) {
//        Select<AUnit> nearbyAllies = Select.ourCombatUnits().inRadius(10, unit);
//        if (nearbyAllies.count() <= 4) {
//            unit.move(nearbyAllies.first().getPosition(), UnitActions.TOGETHER);
//            unit.setTooltip("#MA:Concentrate!");
//            return true;
//        }

        if (unit.distanceTo(focusPoint) > 6) {
//                unit.attackPosition(focusPoint);
            unit.move(focusPoint, UnitActions.MOVE);
            unit.setTooltip("#MA:Forward!");
            return true;
        }

        return false;
    }

}