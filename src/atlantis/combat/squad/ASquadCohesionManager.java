package atlantis.combat.squad;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Count;
import atlantis.units.Select;
import atlantis.units.actions.UnitAction;
import atlantis.units.actions.UnitActions;

public class ASquadCohesionManager {

    public static boolean handle(AUnit unit) {
        if (unit.getSquad().getMission().isMissionAttack()) {
            return false;
        }

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
        if (Count.ourCombatUnits() <= 4) {
            return false;
        }

        if (unit.getSquad().getMission().isMissionAttack()) {
            return false;
        }

        if (!unit.isRunning() && unit.distanceTo(squadCenter(unit)) >= 11) {
            unit.move(squadCenter(unit), UnitActions.MOVE, "Very lonely!");
            return true;
        }

        return false;
    }

    public static double preferredDistToSquadCenter(int squadSize) {
        return Math.max(5.0, 1.3 * Math.sqrt(squadSize));
    }

    // =========================================================

    private static boolean handleShouldSpreadOut(AUnit unit, APosition medianPoint) {
        if (unit.getSquad().size() <= 3) {
            return false;
        }

        Select<AUnit> ourCombatUnits = Select.ourCombatUnits();
        if (
                ourCombatUnits.clone().inRadius(4.5, unit).count() >= 18
                || ourCombatUnits.clone().inRadius(2.7, unit).count() >= 9
                || ourCombatUnits.clone().inRadius(0.6, unit).count() >= 4
        ) {
            return unit.moveAwayFrom(
//                    ourCombatUnits.exclude(unit).nearestTo(unit).getPosition(),
                    medianPoint,
                    1.5,
                    "Spread out"
            );
        }

        return false;
    }

    private static boolean handleShouldStickCloser(AUnit unit, APosition medianPoint) {
        int squadSize = unit.getSquad().size();
        if (squadSize <= 3) {
            return false;
        }

        Select<AUnit> closeFriends = Select.ourCombatUnits().exclude(unit);
        AUnit nearestFriend = closeFriends.clone().nearestTo(unit);
        double maxDistToMedian = preferredDistToSquadCenter(squadSize);

        if (nearestFriend == null) {
            return false;
        }

//        if (
//                nearestFriend != null
//                && unit.distanceTo(nearestFriend) <= 1.2
//                && unit.distanceTo(medianPoint) < optimalDistToMedian
//        ) {
//            return false;
//        }

        if (
                unit.distanceTo(nearestFriend) > 2.3
        ) {
            unit.move(
                    medianPoint.translatePercentTowards(unit, 50),
                    UnitActions.MOVE,
                    "Closer(" + (int) medianPoint.distanceTo(unit) + "/" + (int) unit.distanceTo(nearestFriend) + ")"
            );
            return true;
        }

        if (
                unit.distanceTo(medianPoint) > maxDistToMedian
                && unit.distanceTo(nearestFriend) > 3
        ) {
            unit.move(
                    medianPoint.translatePercentTowards(unit, 50),
                    UnitActions.MOVE,
                    "ComeBack(" + (int) medianPoint.distanceTo(unit) + "/" + (int) unit.distanceTo(nearestFriend) + ")"
            );
            return true;
        }

        if (
//                closeFriends.clone().inRadius(2, unit).count() == 0
                nearestFriend != null
                && (squadSize >= 5 && closeFriends.clone().inRadius(4, unit).count() <= 1)
        ) {
            unit.move(
                    nearestFriend.getPosition().translatePercentTowards(unit, 50),
                    UnitActions.MOVE,
                    "Love(" + (int) nearestFriend.distanceTo(unit) + ")"
            );
            return true;
        }

        return false;
    }

    private static APosition squadCenter(AUnit unit) {
        return unit.getSquad().getSquadCenter();
    }

}
