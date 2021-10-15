package atlantis.combat.squad;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitAction;
import atlantis.units.actions.UnitActions;

public class ASquadCohesionManager {

    public static boolean handle(AUnit unit) {
        APosition medianPosition = unit.getSquad().getSquadCenter();

        if (handleShouldSpreadOut(unit, medianPosition)) {
            return true;
        }

        if (handleShouldStickCloser(unit, medianPosition)) {
            return true;
        }

        return false;
    }

    public static boolean handleExtremeUnitPositioningInSquad(AUnit unit) {
        if (!unit.isRunning() && unit.distanceTo(squadCenter(unit)) >= 11) {
            unit.move(squadCenter(unit), UnitActions.MOVE, "Very lonely!");
            return true;
        }

        return false;
    }

    public static double preferredDistToSquadCenter(int squadSize) {
        return Math.max(4.0, Math.sqrt(squadSize));
    }

    // =========================================================

    private static boolean handleShouldSpreadOut(AUnit unit, APosition medianPoint) {
        if (unit.getSquad().size() <= 1) {
            return false;
        }

        Select<AUnit> ourCombatUnits = Select.ourCombatUnits();
        if (
                ourCombatUnits.clone().inRadius(4.5, unit).count() >= 12
                || ourCombatUnits.clone().inRadius(2.5, unit).count() >= 4
                || ourCombatUnits.clone().inRadius(0.5, unit).count() >= 3
        ) {
            return unit.moveAwayFrom(
//                    ourCombatUnits.exclude(unit).nearestTo(unit).getPosition(),
                    medianPoint,
                    1,
                    "Spread out"
            );
        }

        return false;
    }

    private static boolean handleShouldStickCloser(AUnit unit, APosition medianPoint) {
        int squadSize = unit.getSquad().size();
        if (squadSize <= 1) {
            return false;
        }

        Select<AUnit> closeFriends = Select.ourCombatUnits();
        AUnit nearestFriend = closeFriends.clone().nearestTo(unit);
        double maxDistToMedian = preferredDistToSquadCenter(squadSize);

//        if (
//                nearestFriend != null
//                && unit.distanceTo(nearestFriend) <= 1.2
//                && unit.distanceTo(medianPoint) < optimalDistToMedian
//        ) {
//            return false;
//        }

        if (
                unit.distanceTo(nearestFriend) > 1.9
//        squadSize >= 4 &&
                || (unit.distanceTo(medianPoint) > maxDistToMedian)
                || (squadSize >= 3 && closeFriends.clone().inRadius(4, unit).count() <= 2)
        ) {
            unit.move(
                    medianPoint.translatePercentTowards(unit, 20),
                    UnitActions.MOVE,
                    "Closer(" + (int) medianPoint.distanceTo(unit) + "/" + (int) unit.distanceTo(nearestFriend) + ")"
            );
            return true;
        }

        if (closeFriends.clone().inRadius(2, unit).count() == 0) {
            if (nearestFriend != null) {
                unit.move(
                        nearestFriend.getPosition().translatePercentTowards(unit, 20),
                        UnitActions.MOVE,
                        "Love(" + (int) nearestFriend.distanceTo(unit) + ")"
                );
                return true;
            }
        }

        return false;
    }

    private static APosition squadCenter(AUnit unit) {
        return unit.getSquad().getSquadCenter();
    }

}
