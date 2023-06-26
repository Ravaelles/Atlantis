package atlantis.combat.running;

import atlantis.debug.painter.APainter;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Vector;
import bwapi.Color;

public class RunToPositionFinder {

    protected final ARunningManager running;
    protected final RunInAnyDirection runInAnyDirection = new RunInAnyDirection(this);

    public RunToPositionFinder(atlantis.combat.running.ARunningManager ARunningManager) {
        this.running = ARunningManager;
    }

    /**
     * Running behavior which will make unit run straight away from the enemy.
     */
    protected HasPosition findBestPositionToRun(HasPosition runAwayFrom, double dist) {
        if ((running.setRunTo(running.runTowardsNonStandard.shouldRunTowardsBunker())) != null) {
            return running.runTo();
        }

        if (running.runTowardsNonStandard.shouldRunTowardsBase()) {
            return running.setRunTo(Select.main().position());
        }

        // =========================================================

        // Force units like Marines to slightly run away from each other to avoid one blob of running units
        runAwayFrom = separateEarlyFromFriends(runAwayFrom);

        // === Run directly away from the enemy ========================

        if (running.showBackToEnemy.shouldRunByShowingBackToEnemy()) {
            if (positionForShowingBackToEnemy(runAwayFrom)) return running.runTo();
        }

        // === Run as far from enemy as possible =====================

        return running.setRunTo(runInAnyDirection.runInAnyDirection(runAwayFrom));
    }

    private HasPosition separateEarlyFromFriends(HasPosition runAwayFrom) {
        Selection nearFriends = running.unit().friendsNear().groundUnits().inRadius(1.6, running.unit());
        if (nearFriends.atLeast(1)) {
            AUnit nearestFriend = nearFriends.nearestTo(running.unit());
//            APainter.paintCircleFilled(running.unit, 6, Color.Green);
//            APainter.paintCircleFilled(nearestFriend, 6, Color.Teal);
            runAwayFrom = runAwayFrom.translatePercentTowards(nearestFriend, 40);
        }
        return runAwayFrom;
    }

    private boolean positionForShowingBackToEnemy(HasPosition runAwayFrom) {
        running.setRunTo(findRunPositionShowYourBackToEnemy(runAwayFrom));
        APainter.paintCircleFilled(running.runTo(), 3, Color.Brown);
        APainter.paintLine(running.unit(), running.runTo(), Color.Brown);
        running.unit().setTooltip("ShowBack");

        if (running.runTo() != null) {
            return true;
        }
        return false;
    }

    /**
     * Simplest case: add enemy-to-you-vector to your own position.
     */
    APosition findRunPositionShowYourBackToEnemy(HasPosition runAwayFrom) {
        APosition runTo = showBackToEnemyIfPossible(runAwayFrom);

        if (runTo != null && running.unit().distToMoreThan(runTo, 0.002)) {
            return runTo;
        }

        return null;
    }

    APosition showBackToEnemyIfPossible(HasPosition runAwayFrom) {
        if (true) return null;

        APosition runTo;
        runAwayFrom = runAwayFrom.position();
        double vectorLength = running.unit().distTo(runAwayFrom);
        double runDistInPixels = showBackRunPixelRadius(running.unit(), runAwayFrom);

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

        Vector vector = new Vector(running.unit().x() - runAwayFrom.x(), running.unit().y() - runAwayFrom.y());
        vector.normalize();
        vector.scale(runDistInPixels);

        // Apply opposite 2D vector
        runTo = running.unit().position().translateByVector(vector);

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
        if (isPossibleAndReasonablePosition(
//            running.unit(), runTo, true, "O", "X"
            running.unit(), runTo, true, null, null
        )) {
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
        if (unit.isFlying()) {
            return 1.1;
        }
        else if (unit.isTerranInfantry()) {
            return RunShowBackToEnemy.SHOW_BACK_DIST_TERRAN_INFANTRY * 32;
        }
        else if (unit.isVulture()) {
            return RunShowBackToEnemy.SHOW_BACK_DIST_VULTURE * 32;
        }
        else if (unit.isDragoon()) {
            return RunShowBackToEnemy.SHOW_BACK_DIST_DRAGOON * 32;
        }

        return (RunShowBackToEnemy.SHOW_BACK_DIST_DEFAULT * 32);
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

        // Build list of possible run positions, basically around the clock
        //        APainter.paintCircleFilled(enemyMedian, 8, Color.Purple); // @PAINT EnemyMedian

        //        System.out.println("potentialPositionsList = " + potentialPositionsList.size());

        // =========================================================
        // Find the location that would be most distant to the enemy location

        return runInAnyDirection.findRunPositionInAnyDirection(runAwayFrom);
    }

//    int runAnyDirectionInitialRadius(AUnit unit) {
//
//        return runInAnyDirection.runAnyDirectionInitialRadius(unit, running.runFrom);
//    }

    /**
     * Returns true if given run position is traversable, land-connected and not very, very far
     */
    public boolean isPossibleAndReasonablePosition(
        AUnit unit, APosition position
    ) {
        return isPossibleAndReasonablePosition(unit, position, true, null, null);
    }

    public boolean isPossibleAndReasonablePosition(
        AUnit unit, APosition position, boolean includeNearWalkability, String charForIsOk, String charForNotOk
    ) {
        if (position == null) {
            return false;
        }

        if (unit.isFlying()) {
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

        boolean nearbyWalkable = unit.isFlying() || position.isCloseToMapBounds() || (
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
            && unit.position().groundDistanceTo(position) <= 12
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