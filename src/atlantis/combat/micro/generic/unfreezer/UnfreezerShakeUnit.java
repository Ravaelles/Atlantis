package atlantis.combat.micro.generic.unfreezer;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class UnfreezerShakeUnit {
    public static boolean shake(AUnit unit) {
        if (preventTooManyUnfreezingActions(unit)) return true;

//        if (!unit.isHoldingPosition() && unit.lastActionMoreThanAgo(10, Actions.HOLD_POSITION)) {
        if (!unit.isHoldingPosition()) {
            unit.holdPosition("UnfreezeByHold");
            return true;
        }
//        if (unit.lastActionMoreThanAgo(10, Actions.HOLD_POSITION)) {
//            unit.holdPosition("Unfreeze!!!");
//            return true;
//        }

//        HasPosition goTo = Select.ourBuildings().random();
        HasPosition goTo = unit.enemiesNear().nearestTo(unit);
        if (goTo == null && unit.distToLeader() > 7) goTo = unit.squadLeader();
        if (goTo == null) goTo = unit.friendsNear().notInRadius(2, unit).nearestTo(unit);
//            if (goTo == null) goTo = unit.friendsNear().mostDistantTo(unit);
//            if (goTo == null) goTo = Select.our().exclude(unit).nearestTo(unit);
        if (goTo == null) goTo = Select.our().exclude(unit).groundUnits().random();
        if (goTo == null) goTo = unit.position().translateByPixels(8, 8);

        if (goTo != null) {
            unit.moveTactical(goTo, Actions.MOVE_UNFREEZE, "UnfreezeByMove");
            return true;
        }

        ErrorLog.printErrorOnce("Unfreezing ATTACK unit " + unit + " has no place to go");
        return false;
    }

    private static boolean preventTooManyUnfreezingActions(AUnit unit) {
        return unit.isAccelerating()
            || unit.lastActionLessThanAgo(5, Actions.HOLD_POSITION, Actions.MOVE_UNFREEZE);
    }
}
