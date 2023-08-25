package atlantis.combat.running;

import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

import java.util.ArrayList;

public class RunInAnyDirection {
    public static int ANY_DIRECTION_RADIUS_DEFAULT = 3;
    public static int ANY_DIRECTION_RADIUS_DRAGOON = 4;
    public static int ANY_DIRECTION_RADIUS_TERRAN_INFANTRY = 2;
    public static int ANY_DIRECTION_RADIUS_VULTURE = 4;

    private final RunToPositionFinder positionFinder;

    public RunInAnyDirection(RunToPositionFinder runToPositionFinder) {
        this.positionFinder = runToPositionFinder;
    }

    protected HasPosition runInAnyDirection(HasPosition runAwayFrom) {
        if (
            positionFinder.running.unit()._lastPositionRunInAnyDir != null
            && positionFinder.running.unit().lastActionLessThanAgo(40, Actions.RUN_IN_ANY_DIRECTION)
        ) {
            return positionFinder.running.unit()._lastPositionRunInAnyDir;
        }

        if (positionFinder.running.runTo() == null) {
            positionFinder.running.setRunTo(findRunPositionInAnyDirection(runAwayFrom));
        }
//        if (runTo == null) {
//            fallbackMode = true;
//            runTo = findRunPositionInAnyDirection(runAwayFrom);
//        }

        // =============================================================================


        if (
            positionFinder.running.runTo() != null && positionFinder.running.unit().distTo(positionFinder.running.runTo()) <= 0.02
//                && isPossibleAndReasonablePosition(unit, runTo.position(), true)
        ) {
            // Info: This is a known issue, I couldn't debug this, but it shouldn't be a huge problem...
            System.err.println("Invalid run_any_dir TOO_SHORT, dist = " + positionFinder.running.unit().distTo(positionFinder.running.runTo()));
//            APainter.paintLine(unit, runTo, Color.Purple);
//            APainter.paintLine(
//                    unit.translateByPixels(0, 1),
//                    runTo.translateByPixels(0, 1),
//                    Color.Purple
//            );
            positionFinder.running.setRunTo(findRunPositionInAnyDirection(runAwayFrom));

            if (positionFinder.running.runTo() != null) {
                positionFinder.running.unit().setAction(Actions.RUN_IN_ANY_DIRECTION);
                positionFinder.running.unit()._lastPositionRunInAnyDir = positionFinder.running.runTo();
                APainter.paintCircleFilled(positionFinder.running.runTo(), 3, Color.Green);
                APainter.paintLine(positionFinder.running.unit(), positionFinder.running.runTo(), Color.Green);
                positionFinder.running.unit().setTooltip("RunAnyDir");
            }

//            APainter.paintCircleFilled(runTo, 8, Color.Red);
        }

        // === Run to base as a fallback ===========================

//        if (runTo == null) {
//            runTo = handleRunToMainAsAFallback(unit, runAwayFrom);
//        }

        // =============================================================================

//        System.err.println("Invalid run_any_dir NULL");
        return positionFinder.running.runTo();
    }

    /**
     * Returns a place where run to, searching in all directions, which is walkable, inbounds and most distant
     * to given runAwayFrom position.
     */
    APosition findRunPositionInAnyDirection(HasPosition runAwayFrom) {
        AUnit unit = positionFinder.running.unit;

        if (
            positionFinder.running.runTo != null
            && unit.lastActionLessThanAgo(4, Actions.RUN_IN_ANY_DIRECTION)) {
            return positionFinder.running.runTo.position();
        }

        int radius = runAnyDirectionInitialRadius(unit, runAwayFrom);

        // Build list of possible run positions, basically around the clock
        ArrayList<APosition> potentialPositionsList = new ArrayList<APosition>();
//        APainter.paintCircleFilled(enemyMedian, 8, Color.Purple); // @PAINT EnemyMedian

        positionSearchLoop(radius, potentialPositionsList);
        positionSearchLoop((int) (radius * 0.6), potentialPositionsList);



        // =========================================================
        // Find the location that would be most distant to the enemy location
        double mostDistant = -99;
        APosition bestPosition = null;
        for (APosition position : potentialPositionsList) {

            // Score is calculated as:
            // - being most distant to enemy we're running from,
            AUnit closestAlly = positionFinder.running.unit().friendsNear().nearestTo(positionFinder.running.unit());
            double tooCloseFriendFactor = (closestAlly == null ? 0 : closestAlly.distTo(position) / 10);
            double positionScore = runAwayFrom.distTo(position) - tooCloseFriendFactor;
            if (bestPosition == null || positionScore >= mostDistant) {
                bestPosition = position;
                mostDistant = positionScore;
            }
        }

        return bestPosition;
    }

    private void positionSearchLoop(int radius, ArrayList<APosition> potentialPositionsList) {
        for (int dtx = -radius; dtx <= radius; dtx += 1) {
            for (int dty = -radius; dty <= radius; dty += 1) {
                if (dtx != -radius && dtx != radius && dty != -radius && dty != radius) {
                    continue;
                }

                // Create position, Make sure it's inbounds
//                APosition potentialPosition = unit.translateByTiles(dtx, dty).makeValidFarFromBounds();
                APosition potentialPosition = positionFinder.running.unit().translateByTiles(dtx, dty);

                // If has path to given point, add it to the list of potential points
//                APainter.paintLine(unitPosition, potentialPosition, Color.Purple);
//                if (isPossibleAndReasonablePosition(unit, potentialPosition, false, "v", "x")) {
                if (
                    positionFinder.isPossibleAndReasonablePosition(
                        positionFinder.running.unit(),
                        potentialPosition,
                        false,
                        null,
                        null
//                        "O",
//                        "x"
                    )
                    && !potentialPosition.isCloseToMapBounds()
                ) {
                    potentialPositionsList.add(potentialPosition);
                }
            }
        }
    }

    int runAnyDirectionInitialRadius(AUnit unit, HasPosition runFrom) {
        if (unit.isVulture()) {
            return ANY_DIRECTION_RADIUS_VULTURE;
        }
        else if (unit.isDragoon()) {
            return ANY_DIRECTION_RADIUS_DRAGOON;
        }
        else if (unit.isInfantry()) {
            return A.inRange(2, (int) (unit.distTo(runFrom) * 2), ANY_DIRECTION_RADIUS_TERRAN_INFANTRY);
        }

        return ANY_DIRECTION_RADIUS_DEFAULT;
    }
}
