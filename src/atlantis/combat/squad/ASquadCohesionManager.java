package atlantis.combat.squad;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.squad.positioning.ComeCloser;
import atlantis.combat.squad.positioning.SquadCohesion;
import atlantis.combat.squad.positioning.TooClustered;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

public class ASquadCohesionManager {

    public static boolean update(AUnit unit) {
//        if (shouldSkip(unit)) {
//            return false;
//        }

        if (A.supplyUsed() >= 150 && ArmyStrength.ourArmyRelativeStrength() >= 50) {
            return false;
        }

        if (ComeCloser.handleComeCloser(unit)) {
            return true;
        }

        if (TooClustered.handleTooClustered(unit)) {
            return true;
        }

        if (SquadCohesion.handleTooLowCohesion(unit)) {
            return true;
        }

        return false;
    }

//    private static boolean shouldSkip(AUnit unit) {
//        return
//                unit.isMissionDefend()
//                || unit.squadSize() <= 2
//                || unit.mission().focusPoint() == null
//                || (!unit.isMissionDefend() && unit.distToNearestChokeLessThan(6));
//    }

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
