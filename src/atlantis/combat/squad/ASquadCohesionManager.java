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

        return handleShouldStickCloser(unit);
    }

    private static boolean shouldSkip(AUnit unit) {
        return unit.squad().mission().isMissionAttack();
    }

    public static boolean handleExtremeUnitPositioningInSquad(AUnit unit) {
        if (shouldSkipExtremeUnitPositioning(unit)) {
            return false;
        }

        if (!unit.isRunning() && unit.distTo(squadCenter(unit)) >= 11) {
            unit.move(squadCenter(unit), UnitActions.MOVE, "Ran too far!");
            return true;
        }

        return false;
    }

    private static boolean shouldSkipExtremeUnitPositioning(AUnit unit) {
        if (Us.isProtoss() && Count.ourCombatUnits() <= 4) {
            return true;
        }

        if (unit.squad() == null) {
            return true;
        }

        if (unit.squad().mission().isMissionAttack()) {
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

        if (AGame.timeSeconds() < 350 && ourCombatUnits.clone().inRadius(1.3, unit).count() >= 2) {
            return true;
        }

        if (
                ourCombatUnits.clone().inRadius(4.5, unit).atLeast(25)
                || ourCombatUnits.clone().inRadius(2.7, unit).atLeast(12)
                || ourCombatUnits.clone().inRadius(0.6, unit).atLeast(4)
        ) {
            return unit.moveAwayFrom(
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
        APosition center = squadCenter(unit);

        if (nearestFriend == null) {
            return false;
        }

        if (isNearestFriendTooFar(unit, nearestFriend, center)) {
            return true;
        }

        if (isTooFarFromSquadCenter(unit, nearestFriend, center)) {
            return true;
        }

        if (isSquadQuiteNumerousAndUnitTooFarFromCenter(unit, nearestFriend, closeFriends)) {
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean isSquadQuiteNumerousAndUnitTooFarFromCenter(AUnit unit, AUnit nearestFriend, Select<AUnit> closeFriends) {
        if (
                (unit.squadSize() >= 5 && closeFriends.clone().inRadius(3, unit).count() <= 1)
                        && (unit.squadSize() >= 12 && closeFriends.clone().inRadius(5, unit).count() <= 1)
        ) {
            unit.move(
                    unit.getPosition().translatePercentTowards(nearestFriend, 20),
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
                    unit.getPosition().translatePercentTowards(center, 20),
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
        if (Us.isTerran()) {
            return false;
        }

        return unit.squadSize() <= 3;
    }

    private static APosition squadCenter(AUnit unit) {
        return unit.squad().center();
    }

}
