package atlantis.combat.running;

import atlantis.debug.painter.APainter;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Vector;
import bwapi.Color;

import java.util.ArrayList;

public class RunToPositionFinder {
    private final ARunningManager manager;

    public RunToPositionFinder(atlantis.combat.running.ARunningManager ARunningManager) {
        this.manager = ARunningManager;
    }

    /**
     * Running behavior which will make unit run straight away from the enemy.
     */
    protected HasPosition findBestPositionToRun(HasPosition runAwayFrom, double dist) {
        if ((manager.setRunTo(manager.shouldRunTowardsBunker())) != null) {
            return manager.runTo();
        }

        if (manager.shouldRunTowardsBase()) {
            return manager.setRunTo(Select.main().position());
        }

        // === Run directly away from the enemy ========================

        if (manager.shouldRunByShowingBackToEnemy()) {

            // Force units like Marines to slightly run away from each other to avoid one blob of running units
            Selection nearFriends = manager.unit().friendsNear().groundUnits().inRadius(1.6, manager.unit());
            if (nearFriends.atLeast(1)) {
                AUnit nearestFriend = nearFriends.nearestTo(manager.unit());
//                HasPosition old = runAwayFrom;
                runAwayFrom = runAwayFrom.translatePercentTowards(nearestFriend, 20);
//                System.out.println("nearFriends TRANSLATE " + old.distTo(runAwayFrom));
            }

            manager.setRunTo(findRunPositionShowYourBackToEnemy(runAwayFrom));
            APainter.paintCircleFilled(manager.runTo(), 3, Color.Brown);
            APainter.paintLine(manager.unit(), manager.runTo(), Color.Brown);
            manager.unit().setTooltip("ShowBack");

            if (manager.runTo() != null) {
                return manager.runTo();
            }
        }

        // === Run as far from enemy as possible =====================

        return manager.setRunTo(runInAnyDirection(runAwayFrom));
    }

    protected HasPosition runInAnyDirection(HasPosition runAwayFrom) {
        if (
            manager.unit()._lastPositionRunInAnyDir != null
                && manager.unit().lastActionLessThanAgo(15, Actions.RUN_IN_ANY_DIRECTION)
        ) {
            return manager.unit()._lastPositionRunInAnyDir;
        }

        if (manager.runTo() == null) {
            manager.setRunTo(findRunPositionInAnyDirection(runAwayFrom));
        }
//        if (runTo == null) {
//            fallbackMode = true;
//            runTo = findRunPositionInAnyDirection(runAwayFrom);
//        }

        // =============================================================================

//        System.out.println("runTo = " + runTo + " // " + unit);
        if (
            manager.runTo() != null && manager.unit().distTo(manager.runTo()) <= 0.02
//                && isPossibleAndReasonablePosition(unit, runTo.position(), true)
        ) {
            // Info: This is a known issue, I couldn't debug this, but it shouldn't be a huge problem...
            System.err.println("Invalid run_any_dir TOO_SHORT, dist = " + manager.unit().distTo(manager.runTo()));
//            APainter.paintLine(unit, runTo, Color.Purple);
//            APainter.paintLine(
//                    unit.translateByPixels(0, 1),
//                    runTo.translateByPixels(0, 1),
//                    Color.Purple
//            );
            manager.setRunTo(findRunPositionInAnyDirection(runAwayFrom));

            if (manager.runTo() != null) {
                manager.unit().setAction(Actions.RUN_IN_ANY_DIRECTION);
                manager.unit()._lastPositionRunInAnyDir = manager.runTo();
                APainter.paintCircleFilled(manager.runTo(), 3, Color.Green);
                APainter.paintLine(manager.unit(), manager.runTo(), Color.Green);
                manager.unit().setTooltip("RunAnyDir");
            }

//            APainter.paintCircleFilled(runTo, 8, Color.Red);
        }

        // === Run to base as a fallback ===========================

//        if (runTo == null) {
//            runTo = handleRunToMainAsAFallback(unit, runAwayFrom);
//        }

        // =============================================================================

//        System.err.println("Invalid run_any_dir NULL");
        return manager.runTo();
    }

    /**
     * Simplest case: add enemy-to-you-vector to your own position.
     */
    APosition findRunPositionShowYourBackToEnemy(HasPosition runAwayFrom) {
        APosition runTo = showBackToEnemyIfPossible(runAwayFrom);

        if (runTo != null && manager.unit().distToMoreThan(runTo, 0.002)) {
            return runTo;
        }

        return null;
    }

    APosition showBackToEnemyIfPossible(HasPosition runAwayFrom) {
        APosition runTo;
        runAwayFrom = runAwayFrom.position();
        double vectorLength = manager.unit().distTo(runAwayFrom);
        double runDistInPixels = showBackRunPixelRadius(manager.unit(), runAwayFrom);

        if (vectorLength < 0.01) {
//            CameraManager.centerCameraOn(unit);
//            System.err.println("Serious issue: run vectorLength = " + vectorLength);
//            System.err.println("runner = " + unit + " // " + unit.position());
//            System.err.println("runAwayFrom = " + runAwayFrom);
//            System.err.println("unit.distTo(runAwayFrom) = " + unit.distTo(runAwayFrom));
//            A.printStackTrace();
//            GameSpeed.pauseGame();
            return null;
        }

        Vector vector = new Vector(manager.unit().x() - runAwayFrom.x(), manager.unit().y() - runAwayFrom.y());
        vector.normalize();
        vector.scale(runDistInPixels);

        // Apply opposite 2D vector
        runTo = manager.unit().position().translateByVector(vector);

        // === Ensure position is in bounds ========================================

        int oldX = runTo.getX();
        int oldY = runTo.getY();

        runTo = runTo.makeValidFarFromBounds();

        // If vector changed (meaning we almost reached map boundaries) disallow it
        if (runTo.getX() != oldX && runTo.getY() != oldY) {
//            throw new RuntimeException("aaa " + unit + " // " + unit.position() + " // " + runAwayFrom);
            return null;
        }

        // =========================================================

        // If run distance is acceptably long and it's connected, it's ok.
        if (isPossibleAndReasonablePosition(manager.unit(), runTo, true, "O", "X")) {
//        if (isPossibleAndReasonablePosition(unit, runTo, true, null, null)) {
//            APainter.paintLine(unit.position(), runTo, Color.Purple);
//            APainter.paintLine(unit.translateByPixels(-1, -1), runTo, Color.Purple);
            return runTo;
        }
        else {
//            System.err.println("Not possible to show back");
            return null;
        }
    }

    double showBackRunPixelRadius(AUnit unit, HasPosition runAwayFrom) {
        if (unit.isAir()) {
            return 1.1;
        }
        else if (unit.isTerranInfantry()) {
            return atlantis.combat.running.ARunningManager.SHOW_BACK_DIST_TERRAN_INFANTRY * 32;
        }
        else if (unit.isVulture()) {
            return atlantis.combat.running.ARunningManager.SHOW_BACK_DIST_VULTURE * 32;
        }
        else if (unit.isDragoon()) {
            return atlantis.combat.running.ARunningManager.SHOW_BACK_DIST_DRAGOON * 32;
        }

        return (atlantis.combat.running.ARunningManager.SHOW_BACK_DIST_DEFAULT * 32);
    }

    /**
     * Returns a place where run to, searching in all directions, which is walkable, inbounds and most distant
     * to given runAwayFrom position.
     */
//    private APosition findPositionToRunInAnyDirection(AUnit unit, HasPosition runAwayFrom) {
//
//        // === Define run from ====================================================
////        Units unitsInRadius = Select.enemyRealUnits().melee().inRadius(4, unit).units();
////        APosition runAwayFrom = unitsInRadius.median();
//        if (runAwayFrom == null) {
//            System.err.println("Run away from is null in findRunPositionAtAnyDirection");
//            return null;
//        }
//
//        // === Define if we don't want to go towards region polygon points ========
//
////        boolean avoidCornerPoints = AMap.getDistanceToAnyRegionPolygonPoint(unit.getPosition()) > 1.5;
//
//        // ========================================================================
//
//        APosition unitPosition = unit.position();
//        int radius = runAnyDirectionInitialRadius(unit);
//        APosition bestPosition = null;
//        while (bestPosition == null && radius >= 0.3) {
//            bestPosition = findRunPositionInAnyDirection(unitPosition, runAwayFrom, radius);
//            radius -= 1;
//        }
//
//        // =========================================================
//
////        if (bestPosition != null) {
////            APainter.paintLine(unit, bestPosition, Color.Green);
////            APainter.paintLine(unit.getPosition().translateByPixels(1, 1), bestPosition.translateByPixels(1, 1), Color.Green);
////        }
//
////        AtlantisPainter.paintCircleFilled(unit.getPosition(), 7, Color.Purple);
////        AtlantisPainter.paintLine(unit.getPosition(), bestPosition, Color.Green);
////        AtlantisPainter.paintLine(unit.getPosition().translateByPixels(1, 1), bestPosition.translateByPixels(1, 1), Color.Green);
//        return bestPosition;
//    }
    APosition findRunPositionInAnyDirection(HasPosition runAwayFrom) {
        int radius = runAnyDirectionInitialRadius(manager.unit());

        // Build list of possible run positions, basically around the clock
        ArrayList<APosition> potentialPositionsList = new ArrayList<APosition>();
//        APainter.paintCircleFilled(enemyMedian, 8, Color.Purple); // @PAINT EnemyMedian

        for (int dtx = -radius; dtx <= radius; dtx++) {
            for (int dty = -radius; dty <= radius; dty++) {
                if (dtx != -radius && dtx != radius && dty != -radius && dty != radius) {
                    continue;
                }

                // Create position, Make sure it's inbounds
//                APosition potentialPosition = unit.translateByTiles(dtx, dty).makeValidFarFromBounds();
                APosition potentialPosition = manager.unit().translateByTiles(dtx, dty);

                // If has path to given point, add it to the list of potential points
//                APainter.paintLine(unitPosition, potentialPosition, Color.Purple);
//                if (isPossibleAndReasonablePosition(unit, potentialPosition, false, "v", "x")) {
                if (
                    isPossibleAndReasonablePosition(manager.unit(), potentialPosition, false, null, null)
                        && !potentialPosition.isCloseToMapBounds()
                ) {
                    potentialPositionsList.add(potentialPosition);
                }
            }
        }

//        System.out.println("potentialPositionsList = " + potentialPositionsList.size());

        // =========================================================
        // Find the location that would be most distant to the enemy location
        double mostDistant = -99;
        APosition bestPosition = null;
        for (APosition position : potentialPositionsList) {

            // Score is calculated as:
            // - being most distant to enemy we're running from,
            AUnit closestAlly = manager.unit().friendsNear().nearestTo(manager.unit());
            double tooCloseFriendFactor = (closestAlly == null ? 0 : closestAlly.distTo(position) / 10);
            double positionScore = runAwayFrom.distTo(position) - tooCloseFriendFactor;
            if (bestPosition == null || positionScore >= mostDistant) {
                bestPosition = position;
                mostDistant = positionScore;
            }
        }

        return bestPosition;
    }

    int runAnyDirectionInitialRadius(AUnit unit) {
        if (unit.isVulture()) {
            return atlantis.combat.running.ARunningManager.ANY_DIRECTION_RADIUS_VULTURE;
        }
        else if (unit.isDragoon()) {
            return atlantis.combat.running.ARunningManager.ANY_DIRECTION_RADIUS_DRAGOON;
        }
        else if (unit.isInfantry()) {
            return atlantis.combat.running.ARunningManager.ANY_DIRECTION_RADIUS_TERRAN_INFANTRY;
        }

        return atlantis.combat.running.ARunningManager.ANY_DIRECTION_RADIUS_DEFAULT;
    }

    /**
     * Returns true if given run position is traversable, land-connected and not very, very far
     */
    public boolean isPossibleAndReasonablePosition(
        AUnit unit, APosition position
    ) {
//        return isPossibleAndReasonablePosition(unit, position, true, "#", "%");
        return isPossibleAndReasonablePosition(unit, position, true, null, null);
    }

    public boolean isPossibleAndReasonablePosition(
        AUnit unit, APosition position, boolean includeNearWalkability, String charForIsOk, String charForNotOk
    ) {
        if (position == null) {
            return false;
        }

        if (unit.isAir()) {
            return true;
        }

        position = position.makeWalkable(1);

        if (position == null) {
            return false;
        }

//        System.out.println("position.isWalkable() = " + position.isWalkable());
//        System.out.println("unit.hasPathTo(position) = " + unit.hasPathTo(position));
//        System.out.println("unit.position().groundDistanceTo(position) = " + unit.position().groundDistanceTo(position));
        int walkRadius = 32;

        boolean nearbyWalkable = position.isCloseToMapBounds() || (
            position.translateByPixels(-walkRadius, -walkRadius).isWalkable()
                && position.translateByPixels(walkRadius, walkRadius).isWalkable()
                && position.translateByPixels(walkRadius, -walkRadius).isWalkable()
                && position.translateByPixels(-walkRadius, -walkRadius).isWalkable()
        );
        boolean isWalkable = position.isWalkable() && nearbyWalkable;
//                && (
//                    !includeNearWalkability


        boolean isOkay = isWalkable
//                )
//                && (!includeUnitCheck || Select.our().exclude(this.unit).inRadius(0.6, position).count() <= 0)
//                && Select.ourWithUnfinished().exclude(unit).inRadius(unit.size(), position).count() <= 0
            && Select.all().inRadius(unit.size() * 1.7, position).exclude(unit).isEmpty()
//                && distToNearestRegionBoundaryIsOkay(position)
            && unit.hasPathTo(position)
            && unit.position().groundDistanceTo(position) <= 18
//                && Select.neutral().inRadius(1.2, position).count() == 0
//                && Select.enemy().inRadius(1.2, position).count() == 0
//                && Select.ourBuildings().inRadius(1.2, position).count() == 0
            ;

        if (charForIsOk != null) {
            APainter.paintTextCentered(position, isOkay ? charForIsOk : charForNotOk, isOkay ? Color.Green : Color.Red);
        }

        return isOkay;
    }
}