package atlantis.combat.squad;

import atlantis.combat.missions.AFocusPoint;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

public class TooSpreadOut extends ASquadCohesionManager {

    public static boolean handleTooSpreadOut(AUnit unit) {
        if (shouldSkipTooSpreadOut(unit)) {
            return false;
        }

        if (unit.friendsNearby().inRadius(3, unit).atLeast(7)) {
            return false;
        }

        if (isTooFarFromSquadCenter(unit)) {
            return true;
        }

        if (isTooFarAhead(unit)) {
            return true;
        }

//        APosition squadCenter = squadCenter(unit);
//        if (unit.hasPathTo(squadCenter)) {
////            boolean tooFarFromCenter = unit.distTo(squadCenter) >= maxDistanceToSquadCenter(unit);
////            if (tooFarFromCenter || tooFarForward) {
//            if (unit.mission() != null && unit.mission().focusPoint() != null) {
//                if (tooFarForward(unit, unit.mission().focusPoint(), squadCenter)) {
//                    return unit.move(squadCenter(unit), UnitActions.MOVE, "TooSpread");
//                }
//            }
//
//
////            maxDistanceToSquadCenter(unit)
//        }

        return false;
    }

    private static boolean isTooFarAhead(AUnit unit) {
        if (unit.isMoving() && unit.lastActionLessThanAgo(6)) {
            return false;
        }

        if (unit.groundDist(focusPoint(unit)) > unit.squad().groundDistToFocusPoint() + 3) {
            unit.addLog("TooAhead");
            return true;
        }

        return false;
    }

    private static boolean tooFarForward(AUnit unit, AFocusPoint focusPoint, APosition squadCenter) {
        double unitDistToFocus = unit.groundDist(focusPoint);
        double centerDistToFocus = squadCenter.groundDist(focusPoint);

        return unitDistToFocus - centerDistToFocus <= -5.8;
    }

    // =========================================================

//    private static double maxDistanceToSquadCenter(AUnit unit) {
//        int max = Math.max(2, unit.squadSize() / 5);
//
//        if (unit.isSquadScout()) {
//            max += 1;
//        }
//
//        return max;
//    }

    private static boolean shouldSkipTooSpreadOut(AUnit unit) {
//        if (unit.cooldownAbsolute() > 0) {
//            return true;
//        }

        if (Count.ourCombatUnits() <= 4) {
            return true;
        }

        if (unit.squad() == null) {
            return true;
        }

//        if (unit.squad().mission().isMissionAttack()) {
//            return false;
//        }

        return false;
    }

    // =========================================================

    private static boolean isTooFarFromSquadCenter(AUnit unit) {
        if (unit.squad() == null) {
            return false;
        }

        APosition center = unit.squad().center();
        double maxDistToSquadCenter = preferredDistToSquadCenter(unit.squadSize());

        if (unit.distTo(center) > maxDistToSquadCenter) {
            AUnit nearestFriend = unit.friendsNearby().nearestTo(unit);
            if (nearestFriend == null) {
                return false;
            }

//            if (!unit.recentlyMoved()) {
                unit.move(
                        unit.translatePercentTowards(center, 20),
                        Actions.MOVE_FOCUS,
                        "TooExposed(" + (int) center.distTo(unit) + "/" + (int) unit.distTo(nearestFriend) + ")",
                        false
                );
                return true;
//            }

//            return false;
        }

        return false;
    }

}
