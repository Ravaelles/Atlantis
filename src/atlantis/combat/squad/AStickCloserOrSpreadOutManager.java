package atlantis.combat.squad;

import atlantis.AGame;
import atlantis.debug.APainter;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import bwapi.Color;

public class AStickCloserOrSpreadOutManager {

    public static boolean handle(AUnit unit) {
        APosition medianPosition = unit.getSquad().getMedianUnitPosition();

        if (handleShouldSpreadOut(unit, medianPosition)) {
            return true;
        }

        if (handleShouldStickCloser(unit, medianPosition)) {
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean handleShouldSpreadOut(AUnit unit, APosition medianPoint) {
        Select<AUnit> ourCombatUnits = Select.ourCombatUnits();
        if (
                ourCombatUnits.clone().inRadius(4.5, unit).count() >= 12
                || ourCombatUnits.clone().inRadius(2.5, unit).count() >= 4
        ) {
            return unit.moveAwayFrom(
                    ourCombatUnits.exclude(unit).nearestTo(unit).getPosition(),
                    2,
                    "Spread out"
            );
        }

        return false;
    }

    private static boolean handleShouldStickCloser(AUnit unit, APosition medianPoint) {
        Select<AUnit> closeFriends = Select.ourCombatUnits();
        AUnit nearestFriend = closeFriends.clone().nearestTo(unit);
        int squadSize = unit.getSquad().size();
        double maxDistToMedian = maxDistToMedian(squadSize);

//        if (
//                nearestFriend != null
//                && unit.distanceTo(nearestFriend) <= 1.2
//                && unit.distanceTo(medianPoint) < optimalDistToMedian
//        ) {
//            return false;
//        }

        if (
                unit.distanceTo(nearestFriend) > 1.5
                || unit.distanceTo(medianPoint) > maxDistToMedian
                || (squadSize >= 3 && closeFriends.clone().inRadius(4, unit).count() <= 3)
        ) {
            unit.move(
                    medianPoint,
                    UnitActions.MOVE,
                    "Closer(" + (int) medianPoint.distanceTo(unit) + "/" + (int) unit.distanceTo(nearestFriend) + ")"
            );
            return true;
        }

        if (squadSize >= 2 && closeFriends.clone().inRadius(2, unit).count() == 0) {
            if (nearestFriend != null) {
                unit.move(
                        nearestFriend.getPosition(),
                        UnitActions.MOVE,
                        "Love(" + (int) nearestFriend.distanceTo(unit) + ")"
                );
                return true;
            }
        }

        return false;
    }

    public static double maxDistToMedian(int squadSize) {
        return Math.max(2.6, Math.ceil(squadSize / 5.0));
    }

}
