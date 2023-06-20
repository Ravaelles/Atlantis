package atlantis.combat.squad.positioning;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;

public class ComeCloser extends ASquadCohesionManager {

    public static boolean handleComeCloser(AUnit unit) {
        if (Enemy.terran() && !We.terran()) {
            return false;
        }

        if (shouldSkip(unit)) {
            return false;
        }

        if (
            isTooFarFromSquadCenter(unit)
                || isTooFarFromTanks(unit)
                || isTooFarAhead(unit)
                || isTooFarAhead(unit)
                || TerranInfantryComeCloser.isTooFarFromMedic(unit)
        ) {
            return comeCloser(unit);
        }

//        if (shouldGetCloser(unit)) {
//            return true;
//        }

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

    // =========================================================

    private static boolean comeCloser(AUnit unit) {
        HasPosition squadCenter = unit.squadCenter();
        if (squadCenter != null) {
            HasPosition goTo = unit.distToSquadCenter() >= unit.squadRadius()
                ? squadCenter
                : unit.translateTilesTowards(2, squadCenter).makeWalkable(6);
            if (goTo != null && unit.friendsNear().inRadius(3, unit).atMost(2)) {
                return unit.move(goTo, Actions.MOVE_FORMATION, "Closer", false);
            }
        }

        return false;
    }

    private static boolean isTooFarFromTanks(AUnit unit) {
        if (!We.terran() || unit.isMissionDefend()) {
            return false;
        }

        if (Count.tanks() >= 2) {
            AUnit tank = Select.ourTanks().nearestTo(unit);
            if (tank != null && tank.distToMoreThan(unit, 4.9)) {
                if (unit.move(
                    unit.translateTilesTowards(2, tank),
                    Actions.MOVE_FORMATION,
                    "HugTanks",
                    true
                )) {
                    unit.addLog("HugTanks");
                    return true;
                }
            }
        }

        return false;
    }

    // =========================================================

    private static boolean isTooFarAhead(AUnit unit) {
//        if (unit.isMoving() && unit.lastActionLessThanAgo(6)) {
//            return false;
//        }

        if (unit.cooldownRemaining() == 0) {
            return false;
        }

        if (unit.distToSquadCenter() <= 4 || unit.distToSquadCenter() >= 15) {
            return false;
        }

        AFocusPoint focusPoint = focusPoint(unit);
        if (
            focusPoint != null
            && unit.groundDist(focusPoint) + 3 <= unit.squad().groundDistToFocusPoint()
        ) {
            if (unit.friendsNear().inRadius(4, unit).atMost(5)) {
                if (unit.isMoving() && unit.cooldown() == 0) {
                    unit.addLog("TooAhead");
                    return unit.holdPosition("TooAhead", false);
                }
            }
        }

        return false;
    }

//    private static boolean tooFarForward(AUnit unit, AFocusPoint focusPoint, APosition squadCenter) {
//        double unitToFocus = unit.groundDist(focusPoint);
//        double centerDistToFocus = squadCenter.groundDist(focusPoint);
//
//        return unitToFocus - centerDistToFocus <= -5.8;
//    }

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

    private static boolean shouldSkip(AUnit unit) {
//        if (unit.cooldownAbsolute() > 0) {
//            return true;
//        }

        AFocusPoint focusPoint = focusPoint(unit);

        if (focusPoint == null) {
            return false;
        }

        if (unit.isAir()) {
            return true;
        }

        if (unit.squad() == null) {
            return true;
        }

        if (unit.isVulture()) {
            return true;
        }

        if (Count.ourCombatUnits() <= 4) {
            return true;
        }

        if (unit.meleeEnemiesNearCount(3) > 0) {
            return true;
        }

        if (unit.enemiesNear().inRadius(6, unit).count() >= 5) {
            return true;
        }

        return false;
    }

    // =========================================================

    public static boolean isTooFarFromSquadCenter(AUnit unit) {
        if (unit.squad() == null || unit.isTank()) {
            return false;
        }

        if (unit.isMissionAttack()) {
            return false;
        }

        if (unit.distToSquadCenter() >= 15) {
            return false;
        }

        APosition center = unit.squad().center();
        if (center == null) {
            return false;
        }

        double maxDistToSquadCenter = SquadCohesion.squadMaxRadius(unit.squad());

        if (unit.distTo(center) > maxDistToSquadCenter && unit.friendsNear().inRadius(3.5, unit).atMost(5)) {
            AUnit nearestFriend = unit.friendsNear().nearestTo(unit);
            if (nearestFriend == null) {
                return false;
            }

            Selection enemiesNear = unit.enemiesNear();
            if ((unit.isVulture() || unit.isDragoon()) && (enemiesNear.isEmpty() || enemiesNear.onlyMelee())) {
                return false;
            }

            if (unit.move(
                unit.translateTilesTowards(center, 2).makeWalkable(5),
                Actions.MOVE_FOCUS,
                "TooExposed(" + (int) center.distTo(unit) + "/" + (int) unit.distTo(nearestFriend) + ")",
                false
            )) {
                unit.addLog("TooExposed");
                return true;
            }
        }

        return false;
    }

}
