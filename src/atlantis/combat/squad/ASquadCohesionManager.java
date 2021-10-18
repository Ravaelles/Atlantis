package atlantis.combat.squad;

import atlantis.AGame;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Count;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.util.Us;

public class ASquadCohesionManager {

    public static boolean handle(AUnit unit) {
        if (shouldSkip(unit)) {
            return false;
        }

        if (handleShouldSpreadOut(unit)) {
            return true;
        }

        if (handleShouldStickCloser(unit)) {
            return true;
        }

        return false;
    }

    private static boolean shouldSkip(AUnit unit) {
        return unit.squad().getMission().isMissionAttack();
    }

    public static boolean handleExtremeUnitPositioningInSquad(AUnit unit) {
        if (shouldSkipExtremeUnitPositioning(unit)) {
            return false;
        }

        if (!unit.isRunning() && unit.distanceTo(squadCenter(unit)) >= 11) {
            unit.move(squadCenter(unit), UnitActions.MOVE, "Very lonely!");
            return true;
        }

        return false;
    }

    private static boolean shouldSkipExtremeUnitPositioning(AUnit unit) {
        if (Us.isProtoss() && Count.ourCombatUnits() <= 4) {
            return true;
        }

        if (unit.squad().getMission().isMissionAttack()) {
            return false;
        }

        if (unit.equals(unit.squad().getSquadScout())) {
            return false;
        }

        return false;
    }

    public static double preferredDistToSquadCenter(int squadSize) {
        return Math.max(5.0, 1.3 * Math.sqrt(squadSize));
    }

    // =========================================================

    private static boolean handleShouldSpreadOut(AUnit unit) {
        if (unit.squad().size() <= 1) {
            return false;
        }

        Select<AUnit> ourCombatUnits = Select.ourCombatUnits();

        if (AGame.getTimeSeconds() < 350 && ourCombatUnits.clone().inRadius(1.3, unit).count() >= 2) {
            return true;
        }

        if (
                ourCombatUnits.clone().inRadius(4.5, unit).count() >= 24
                || ourCombatUnits.clone().inRadius(2.7, unit).count() >= 12
                || ourCombatUnits.clone().inRadius(0.6, unit).count() >= 4
        ) {
            return unit.moveAwayFrom(
//                    ourCombatUnits.exclude(unit).nearestTo(unit).getPosition(),
                    squadCenter(unit),
                    1.5,
                    "Spread out"
            );
        }

        return false;
    }

    private static boolean handleShouldStickCloser(AUnit unit) {
        if (shouldSkipStickCloser(unit)) {
            return false;
        }

        int squadSize = unit.squad().size();
        Select<AUnit> closeFriends = Select.ourCombatUnits().exclude(unit);
        AUnit nearestFriend = closeFriends.clone().nearestTo(unit);
        double maxDistToMedian = preferredDistToSquadCenter(squadSize);

        if (nearestFriend == null) {
            return false;
        }

//        if (
//                nearestFriend != null
//                && unit.distanceTo(nearestFriend) <= 1.2
//                && unit.distanceTo(center) < optimalDistToMedian
//        ) {
//            return false;
//        }

        APosition center = squadCenter(unit);
        if (
                unit.distanceTo(nearestFriend) > 2.3
        ) {
            unit.move(
                    center.translatePercentTowards(unit, 50),
                    UnitActions.MOVE,
                    "Closer(" + (int) center.distanceTo(unit) + "/" + (int) unit.distanceTo(nearestFriend) + ")"
            );
            return true;
        }

        if (
                unit.distanceTo(center) > maxDistToMedian
                && unit.distanceTo(nearestFriend) > 3
        ) {
            unit.move(
                    unit.getPosition().translatePercentTowards(center, 20),
                    UnitActions.MOVE,
                    "ComeBack(" + (int) center.distanceTo(unit) + "/" + (int) unit.distanceTo(nearestFriend) + ")"
            );
            return true;
        }

        if (
//                closeFriends.clone().inRadius(2, unit).count() == 0
                nearestFriend != null && (
                    (squadSize >= 5 && closeFriends.clone().inRadius(3, unit).count() <= 1)
                    && (squadSize >= 12 && closeFriends.clone().inRadius(7, unit).count() <= 1)
                )
        ) {
            unit.move(
                    unit.getPosition().translatePercentTowards(nearestFriend, 20),
                    UnitActions.MOVE,
                    "Love(" + (int) nearestFriend.distanceTo(unit) + ")"
            );
            return true;
        }

        return false;
    }

    private static boolean shouldSkipStickCloser(AUnit unit) {
        if (Us.isTerran()) {
            return false;
        }

        return unit.squadSize() <= 3;
    }

    private static APosition squadCenter(AUnit unit) {
        return unit.squad().getSquadCenter();
    }

}
