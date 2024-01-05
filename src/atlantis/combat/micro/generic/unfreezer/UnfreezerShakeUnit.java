package atlantis.combat.micro.generic.unfreezer;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class UnfreezerShakeUnit {
    public static boolean shake(AUnit unit) {
        if (unit.lastActionMoreThanAgo(10, Actions.HOLD_POSITION)) {
            unit.holdPosition("Unfreeze!!!");
            return true;
        }

        AUnit goTo = Select.ourBuildings().random();
//            if (goTo == null) goTo = unit.friendsNear().mostDistantTo(unit);
//            if (goTo == null) goTo = Select.our().exclude(unit).nearestTo(unit);
        if (goTo == null) goTo = Select.our().exclude(unit).groundUnits().random();
        if (goTo != null) {
            unit.moveTactical(goTo, Actions.MOVE_UNFREEZE, "Unfreezing");
            return true;
        }

        ErrorLog.printErrorOnce("Unfreezing ATTACK unit " + unit + " has no place to go");
        return false;
    }
}
