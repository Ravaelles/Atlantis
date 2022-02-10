package atlantis.combat.squad;

import atlantis.combat.missions.AFocusPoint;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

public class ASquadCohesionManager {

    public static boolean handle(AUnit unit) {
        if (shouldSkip(unit)) {
            return false;
        }

//        if (unit.isRunning()) {
//            return false;
//        }

        if (ComeCloser.handleComeCloser(unit)) {
            return true;
        }

        if (TooClustered.handleTooClustered(unit)) {
            return true;
        }

        return false;
    }

    private static boolean shouldSkip(AUnit unit) {
        return
//                unit.recentlyMoved(30)
                // Only mission contain enforces unit coordination
//                (unit.mission() != null && !unit.isMissionDefend())
                (unit.mission() != null && unit.isMissionDefend())
//                unit.mission() != null && (unit.mission().isMissionAttack() || unit.isMissionDefend());
//                || unit.friendsNear().atMost(2);
                || unit.squadSize() <= 2
                || unit.mission().focusPoint() == null;
    }

    protected static AFocusPoint focusPoint(AUnit unit) {
        return unit.mission().focusPoint();
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
