package atlantis.combat.micro.generic.unfreezer;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class UnfreezerShakeUnit {
    public static void shake(AUnit unit) {
//        AFocusPoint focusPoint = unit.micro().focusPoint();
//        if (focusPoint != null && unit.distTo(focusPoint) >= 3) {
//            unit.moveTactical(focusPoint, Actions.MOVE_UNFREEZE, "Unfreeze");
//        }
//        else {
            AUnit goTo = Select.ourBuildings().random();
//            if (goTo == null) goTo = unit.friendsNear().mostDistantTo(unit);
//            if (goTo == null) goTo = Select.our().exclude(unit).nearestTo(unit);
            if (goTo == null) goTo = Select.our().exclude(unit).groundUnits().random();
            if (goTo != null) {
                unit.moveTactical(goTo, Actions.MOVE_UNFREEZE, "Unfreezing");
            }
            else {
                ErrorLog.printErrorOnce("Unfreezing ATTACK unit " + unit + " has no place to go");
            }
//        }
    }
}
