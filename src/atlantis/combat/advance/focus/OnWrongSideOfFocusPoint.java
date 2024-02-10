package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class OnWrongSideOfFocusPoint extends MissionManager {
    public OnWrongSideOfFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return focusPoint.isAroundChoke()
            && unit.isGroundUnit();
    }

    protected Manager handle() {
        if (handleWrongSideOfFocus()) {
            return usedManager(this);
        }

        return null;
    }

    /**
     * Unit is too far from its focus point and/or is on the wrong side of it (most evident on ramps).
     */
    private boolean handleWrongSideOfFocus() {
        APosition withdrawTo = focusPoint.fromSide();

        if (!focusPoint.isAroundChoke() || withdrawTo == null) return false;

        double distToFocusPoint = unit.distToFocusPoint();
//        double optimalDist = unit.mission().optimalDist();

        boolean onValidSideOfChoke = IsOnValidSideOfChoke.check(unit, focusPoint);
//        APainter.paintCircleFilled(unit, 6, onValidSideOfChoke ? Color.Green : Color.Red);

        if (!onValidSideOfChoke && distToFocusPoint <= 7) {
            makeFriendsHelpWithdraw(unit, focusPoint);

            unit.move(withdrawTo, Actions.MOVE_FOCUS, "Withdraw", true);
            return true;
        }

//        if (!isOnValidSideOfChoke(unit, focus) && distToFocusPoint <= 7) {
////        if (
////            distToFocusPoint < optimalDist
////                || (!isOnValidSideOfChoke(unit, focus) && distToFocusPoint <= 7)
////        ) {
//        if (makeFriendsHelpWithdraw(unit, focus)) return true;
//
//            unit.move(focusPoint.fromSide(), Actions.MOVE_FOCUS, "Withdraw", true);
//            return true;
//        }

        return false;
    }

    private boolean makeFriendsHelpWithdraw(AUnit unit, AFocusPoint focus) {
        if (unit.enemiesNear().combatUnits().inRadius(8, unit).empty()) {
            for (AUnit friend : unit.friendsNear().inRadius(5, unit).combatUnits().list()) {
                APosition withdrawFriendTo = friend.translateTilesTowards(2, focusPoint.fromSide());
                if (friend.move(withdrawFriendTo, Actions.MOVE_FOCUS, "HelpWithdraw", true)) {
                    friend.setTooltip("HelpWithdraw", true);
                }
                return true;
            }
        }
        return false;
    }
}

