package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import bwapi.Color;

public class OnWrongSideOfFocusPoint extends MissionManager {
    private double margin;

    public OnWrongSideOfFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (focus == null || focus.fromSide() == null || !focus.isAroundChoke() || focus.choke() == null) return false;

        if (unit.eval() >= (unit.isMissionSparta() ? 1.2 : 3)) {
            if (Count.dragoons() >= 3 && unit.isMissionDefendOrSparta()) return false;
            if (unit.isDragoon() && unit.hp() >= 100) return false;
        }

        AChoke choke = focus.choke();
        if (choke == null) return false;
        if (unit.distToFocusPoint() >= 15) return false;

        double unitToFrom = unit.groundDist(focus.fromSide());
        double chokeToFrom = choke.center().groundDist(focus.fromSide());

//        margin = unitToFrom + 2 + choke.width() - chokeToFrom;
        margin = chokeToFrom - unitToFrom - (unit.isRanged() ? 0.2 : 0);
//        absoluteOnWrongSide = unitToFrom < chokeToFrom;

//        unit.paintTextCentered(A.digit(margin) + "", Color.Green, -1.6);
//        if (unit.isLeader()) System.out.println("@" + A.now() + ": " + margin);

        int bonus = unit.isActiveManager(OnWrongSideOfFocusPoint.class) ? 3 : 0;
        return margin < (bonus + (unit.isRanged() ? 1 : 0));

//        && (unit.groundDist(focusPoint.fromSide()) - 4) < focusPoint.groundDist(focusPoint.fromSide());
//        return focusPoint.isAroundChoke()
//            && unit.isGroundUnit();
    }

    protected Manager handle() {
//        System.err.println("@ " + A.now() + " - " + unit.idWithHash() + " - WROOOOOOOOOOOOOOOOONG");
//        if (unit.isLeader()) System.out.println("leader: " + margin + " / " +unit.isMoving());

//        if (margin <= -0.3) {
//            if (unit.isMoving()) {
//                if (unit.isLeader()) System.out.println("stahp");
//                unit.stop("WrongSideStop");
//                return usedManager(this);
//            }
//            else {
//                if (unit.isLeader()) System.out.println("leader NOT moving? " + unit.isMoving());
//            }
//        }
//        else {
            if (withdrawFromWrongSideOfFocus()) {
                return usedManager(this);
            }
//        }

        return null;
    }

    /**
     * Unit is too far from its focus point and/or is on the wrong side of it (most evident on ramps).
     */
    private boolean withdrawFromWrongSideOfFocus() {
        if (margin <= 4 && unit.isMoving() && A.everyNthGameFrame(9)) {
            unit.stop("WrongSideStop");
            return true;
        }

        APosition withdrawTo = focus.fromSide().position();
        if (!focus.isAroundChoke() || withdrawTo == null) return false;

        double distToFocusPoint = unit.distToFocusPoint();
//        double optimalDist = unit.mission().optimalDist();

        boolean onValidSideOfChoke = IsOnValidSideOfChoke.check(unit, focus);

        if (
            distToFocusPoint <= 6
                && ((distToFocusPoint <= 2 && unit.isDragoon()) || !onValidSideOfChoke)
        ) {
            unit.paintCircleFilled(6, Color.Red);
            makeFriendsHelpWithdraw(unit, focus);

//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " WRONG: " + distToFocusPoint);
            unit.move(withdrawTo, Actions.MOVE_FOCUS, "Withdraw");
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
        HasPosition withdrawFriendTo = focus.fromSide();
        if (withdrawFriendTo == null) return false;
        if (unit.enemiesNear().combatUnits().inRadius(4, unit).notEmpty()) return false;

        for (AUnit friend : Select.our().combatUnits().groundUnits().inRadius(2, focus).list()) {
//                APosition withdrawFriendTo = friend.translateTilesTowards(2, focusPoint.fromSide());
            if (friend.move(withdrawFriendTo, Actions.MOVE_FOCUS, "HelpWithdraw", true)) {
                friend.setTooltip("HelpWithdraw", true);
            }
            return true;
        }

        return false;
    }
}

