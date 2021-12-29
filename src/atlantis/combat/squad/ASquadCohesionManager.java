package atlantis.combat.squad;

import atlantis.combat.missions.AFocusPoint;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.actions.UnitActions;
import atlantis.util.We;

public class ASquadCohesionManager {

    public static boolean handle(AUnit unit) {
        if (shouldSkip(unit)) {
            return false;
        }

        if (unit.isRunning()) {
            return false;
        }

        if (TooSpreadOut.handleTooSpreadOut(unit)) {
            return true;
        }

        if (ShouldSpreadOut.handleSpreadOut(unit)) {
            return true;
        }

        return false;
    }

    private static boolean shouldSkip(AUnit unit) {
        return
                // Only mission contain enforces unit coordination
                (unit.mission() != null && !unit.mission().isMissionDefend())
//                unit.mission() != null && (unit.mission().isMissionAttack() || unit.mission().isMissionDefend());
                || unit.friendsNearby().atMost(2);
    }

//    private static boolean handleShouldStickCloser(AUnit unit) {
//        if (shouldSkipStickCloser(unit)) {
//            return false;
//        }
//
//        Selection closeFriends = Select.ourCombatUnits().exclude(unit);
//        AUnit nearestFriend = closeFriends.clone().nearestTo(unit);
//        APosition center = squadCenter(unit);
//
//        if (nearestFriend == null) {
//            return false;
//        }
//
//        if (isNearestFriendTooFar(unit, nearestFriend, center)) {
//            return true;
//        }
//
//        if (isTooFarFromSquadCenter(unit, nearestFriend, center)) {
//            return true;
//        }
//
//        if (isSquadQuiteNumerousAndUnitTooFarFromCenter(unit, nearestFriend, closeFriends)) {
//            return true;
//        }
//
//        return false;
//    }

    // =========================================================

    public static double preferredDistToSquadCenter(int squadSize) {
        return Math.max(5.0, 1.3 * Math.sqrt(squadSize));
    }

//    private static boolean isSquadQuiteNumerousAndUnitTooFarFromCenter(AUnit unit, AUnit nearestFriend, Selection closeFriends) {
//        if (
//                (unit.squadSize() >= 5 && closeFriends.clone().inRadius(3, unit).count() <= 1)
//                        && (unit.squadSize() >= 12 && closeFriends.clone().inRadius(5, unit).count() <= 1)
//        ) {
//            unit.move(
//                    unit.translatePercentTowards(nearestFriend, 20),
//                    UnitActions.MOVE,
//                    "Together(" + (int) nearestFriend.distTo(unit) + ")"
//            );
//            return true;
//        }
//
//        return false;
//    }
//
//    private static boolean isNearestFriendTooFar(AUnit unit, AUnit nearestFriend, APosition center) {
//        if (
//                unit.distTo(nearestFriend) > 2.3
//        ) {
//            unit.move(
//                    center.translatePercentTowards(unit, 50),
//                    UnitActions.MOVE,
//                    "Closer(" + (int) center.distTo(unit) + "/" + (int) unit.distTo(nearestFriend) + ")"
//            );
//            return true;
//        }
//
//        return false;
//    }
//
//    private static boolean shouldSkipStickCloser(AUnit unit) {
//        if (We.terran()) {
//            return false;
//        }
//
//        return unit.squadSize() <= 3;
//    }

    protected static APosition squadCenter(AUnit unit) {
        return unit.squad().center();
    }

}
