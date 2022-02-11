package atlantis.combat.squad;

import atlantis.combat.missions.AFocusPoint;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

public class ASquadCohesionManager {

    public static boolean handle(AUnit unit) {
        if (shouldSkip(unit)) {
            return false;
        }

        if (ComeCloser.handleComeCloser(unit)) {
            return true;
        }

        if (TooClustered.handleTooClustered(unit)) {
            return true;
        }

        if (SquadCohesionAssurance.handleTooLowCohesion(unit)) {
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

    protected static APosition squadCenter(AUnit unit) {
        return unit.squad().center();
    }

}
