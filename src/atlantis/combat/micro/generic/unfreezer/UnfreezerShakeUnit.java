package atlantis.combat.micro.generic.unfreezer;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class UnfreezerShakeUnit {
    public static boolean shake(AUnit unit) {
        if (shouldNotDoAnythingButContinue(unit)) return true;

//        if (!unit.isHoldingPosition() && unit.lastActionMoreThanAgo(10, Actions.HOLD_POSITION)) {
        if (!unit.isHoldingPosition()) {
            unit.holdPosition("UnfreezeByHold");
            return true;
        }
//        if (unit.lastActionMoreThanAgo(10, Actions.HOLD_POSITION)) {
//            unit.holdPosition("Unfreeze!!!");
//            return true;
//        }

        HasPosition goTo = goToPositionNearby(unit);
//        HasPosition goTo = Select.ourBuildings().random();
//        HasPosition goTo = unit.enemiesNear().nearestTo(unit);
//        if (goTo == null && unit.distToLeader() > 3) goTo = unit.squadLeader();
//        if (goTo == null) goTo = unit.friendsNear().notInRadius(2, unit).nearestTo(unit);
//            if (goTo == null) goTo = unit.friendsNear().mostDistantTo(unit);
        if (goTo == null) goTo = Select.our().combatUnits().exclude(unit).nearestTo(unit);
//        if (goTo == null) goTo = Select.our().exclude(unit).groundUnits().random();
//        if (goTo == null) goTo = goToPositionNearby(unit);

        if (goTo != null) {
            unit.moveTactical(goTo, Actions.MOVE_UNFREEZE, "UnfreezeByMove");
            return true;
        }

        ErrorLog.printErrorOnce("Unfreezing ATTACK unit " + unit + " has no place to go");
        return false;
    }

    private static APosition goToPositionNearby(AUnit unit) {
        int moduloId = unit.id() % 5;
        return unit.position().translateByPixels(-16 + moduloId * 8, 16 - moduloId * 8);
    }

    private static boolean shouldNotDoAnythingButContinue(AUnit unit) {
        return unit.isAccelerating()
            || unit.lastActionLessThanAgo(7, Actions.HOLD_POSITION, Actions.MOVE_UNFREEZE);
    }
}
