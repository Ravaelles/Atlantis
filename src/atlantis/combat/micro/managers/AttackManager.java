package atlantis.combat.micro.managers;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public class AttackManager {

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