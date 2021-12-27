package atlantis.combat.squad;

import atlantis.combat.missions.AFocusPoint;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class TooSpreadOut extends ASquadCohesionManager {

    public static boolean handleTooSpreadOut(AUnit unit) {
        if (shouldSkipTooSpreadOut(unit)) {
            return false;
        }

        if (unit.friendsNearby().inRadius(5, unit).atLeast(7)) {
            return false;
        }

        APosition squadCenter = squadCenter(unit);
        if (unit.hasPathTo(squadCenter)) {
//            boolean tooFarFromCenter = unit.distTo(squadCenter) >= maxDistanceToSquadCenter(unit);
//            if (tooFarFromCenter || tooFarForward) {
            if (unit.mission() != null && unit.mission().focusPoint() != null) {
                if (tooFarForward(unit, unit.mission().focusPoint(), squadCenter)) {
                    return unit.move(squadCenter(unit), UnitActions.MOVE, "TooSpread");
                }
            }


//            maxDistanceToSquadCenter(unit)
        }

        return false;
    }

    private static boolean tooFarForward(AUnit unit, AFocusPoint focusPoint, APosition squadCenter) {
        double unitDistToFocus = unit.groundDist(focusPoint);
        double centerDistToFocus = squadCenter.groundDist(focusPoint);

        return unitDistToFocus - centerDistToFocus <= -5.8;
    }

    // =========================================================

    private static double maxDistanceToSquadCenter(AUnit unit) {
        int max = Math.max(2, unit.squadSize() / 5);

        if (unit.isSquadScout()) {
            max += 1;
        }

        return max;
    }

    private static boolean shouldSkipTooSpreadOut(AUnit unit) {
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

}
