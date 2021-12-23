package atlantis.combat.squad;

import atlantis.AGame;
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

        if (handleExtremeUnitPositioningInSquad(unit)) {
            return true;
        }

        if (handleShouldSpreadOut(unit)) {
            return true;
        }

        return false;
    }

    private static boolean shouldSkip(AUnit unit) {
        return unit.mission() != null && (unit.mission().isMissionAttack() || unit.mission().isMissionDefend());
    }

    public static boolean handleExtremeUnitPositioningInSquad(AUnit unit) {
        if (shouldSkipExtremeUnitPositioning(unit)) {
            return false;
        }

        APosition squadCenter = squadCenter(unit);
        if (!unit.isRunning() && unit.distTo(squadCenter) >= maxDistanceToSquadCenter(unit) && unit.hasPathTo(squadCenter)) {
            return unit.move(squadCenter(unit), UnitActions.MOVE, "TooSpread");
        }

        return false;
    }

    private static double maxDistanceToSquadCenter(AUnit unit) {
        int max = Math.max(2, unit.squadSize() / 5);

        if (unit.isSquadScout()) {
            max += 1;
        }

        return max;
    }

    private static boolean shouldSkipExtremeUnitPositioning(AUnit unit) {
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

    public static double preferredDistToSquadCenter(int squadSize) {
        return Math.max(5.0, 1.3 * Math.sqrt(squadSize));
    }

    // =========================================================

    private static boolean handleShouldSpreadOut(AUnit unit) {
        if (unit.squad().size() <= 1 || unit.isMoving()) {
            return false;
        }

        Selection ourCombatUnits = Select.ourCombatUnits().inRadius(5, unit);
        AUnit nearestBuddy = ourCombatUnits.clone().nearestTo(unit);

        if (nearestBuddy != null && nearestBuddy.distToLessThan(unit, 0.5)) {
            return unit.moveAwayFrom(nearestBuddy, 0.5, "Spread out");
        }

        return false;
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

    private static boolean isSquadQuiteNumerousAndUnitTooFarFromCenter(AUnit unit, AUnit nearestFriend, Selection closeFriends) {
        if (
                (unit.squadSize() >= 5 && closeFriends.clone().inRadius(3, unit).count() <= 1)
                        && (unit.squadSize() >= 12 && closeFriends.clone().inRadius(5, unit).count() <= 1)
        ) {
            unit.move(
                    unit.translatePercentTowards(nearestFriend, 20),
                    UnitActions.MOVE,
                    "Together(" + (int) nearestFriend.distTo(unit) + ")"
            );
            return true;
        }

        return false;
    }

    private static boolean isTooFarFromSquadCenter(AUnit unit, AUnit nearestFriend, APosition center) {
        double maxDistToSquadCenter = preferredDistToSquadCenter(unit.squadSize());

        if (
                unit.distTo(center) > maxDistToSquadCenter
                        && unit.distTo(nearestFriend) > 3
        ) {
            unit.move(
                    unit.translatePercentTowards(center, 20),
                    UnitActions.MOVE,
                    "StickTogether(" + (int) center.distTo(unit) + "/" + (int) unit.distTo(nearestFriend) + ")"
            );
            return true;
        }

        return false;
    }

    private static boolean isNearestFriendTooFar(AUnit unit, AUnit nearestFriend, APosition center) {
        if (
                unit.distTo(nearestFriend) > 2.3
        ) {
            unit.move(
                    center.translatePercentTowards(unit, 50),
                    UnitActions.MOVE,
                    "Closer(" + (int) center.distTo(unit) + "/" + (int) unit.distTo(nearestFriend) + ")"
            );
            return true;
        }

        return false;
    }

    private static boolean shouldSkipStickCloser(AUnit unit) {
        if (We.terran()) {
            return false;
        }

        return unit.squadSize() <= 3;
    }

    private static APosition squadCenter(AUnit unit) {
        return unit.squad().center();
    }

}
