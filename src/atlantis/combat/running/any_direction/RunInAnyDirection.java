package atlantis.combat.running.any_direction;

import atlantis.combat.running.ARunningManager;
import atlantis.combat.running.RunToPositionFinder;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.Color;

import java.util.ArrayList;

public class RunInAnyDirection {
    public static int ANY_DIRECTION_RADIUS_DEFAULT = 4;
    public static int ANY_DIRECTION_RADIUS_DRAGOON = 9;
    public static int ANY_DIRECTION_RADIUS_TERRAN_INFANTRY = 5;
    public static int ANY_DIRECTION_RADIUS_VULTURE = 4;

    private AUnit unit = null;
    private ARunningManager runningManager;
    private RunToPositionFinder positionFinder;

    public RunInAnyDirection(RunToPositionFinder positionFinder) {
        this.positionFinder = positionFinder;
    }

    public HasPosition runInAnyDirection(HasPosition runAwayFrom) {
        this.runningManager = positionFinder.running();
        this.unit = runningManager.unit();

        // =========================================================

        runningManager.setRunTo(findRunPositionInAnyDirection(runAwayFrom));

//        System.err.println("findRunPositionInAnyDirection(runAwayFrom) = " + findRunPositionInAnyDirection(runAwayFrom));
//        System.err.println("runAwayFrom = " + runAwayFrom);
//        System.err.println("unit = " + unit);
//        System.err.println("-----------------");

        // =============================================================================

        if (runningManager.runTo() != null) {
            Color color = Color.Purple;
            APainter.paintLine(unit, runningManager.runTo(), color);
            APainter.paintLine(
                unit.translateByPixels(0, 1),
                runningManager.runTo().translateByPixels(0, 1),
                color
            );
        }

        if (handleInvalidCaseWhenRunToIsTooClose()) return runningManager.setRunTo(null);

        // =============================================================================

//        System.err.println("Invalid run_any_dir NULL");
        return runningManager.runTo();
    }

    private static double evalPosition(AUnit unit, APosition position, HasPosition runAwayFrom) {
        return 1.3 * position.distTo(runAwayFrom)
            - position.distTo(unit)
            - Select.all().inRadius(1.0, position).exclude(unit).count() * 0.5;
    }

    private boolean handleInvalidCaseWhenRunToIsTooClose() {
        if (
            runningManager.runTo() != null
                && unit.distTo(runningManager.runTo().position()) <= 0.05
//                && isPossibleAndReasonablePosition(unit, runningManager.runTo().position(), true)
        ) {
            // Info: This is a known issue, I couldn't debug this, but it shouldn't be a huge problem...
            System.err.println("Invalid run_any_dir TOO_SHORT, dist = " + unit.distTo(runningManager.runTo()));
            AAdvancedPainter.paintCircleFilled(unit, 7, Color.Red);
//            CameraCommander.centerCameraOn(unit);

//            runningManager.setRunTo(findRunPositionInAnyDirection(runAwayFrom));
//
//            if (runningManager.runTo() != null) {
//                unit.setAction(Actions.RUN_IN_ANY_DIRECTION);
//                unit._lastPositionRunInAnyDir = runningManager.runTo();
//                APainter.paintCircleFilled(runningManager.runTo(), 3, Color.Green);
//                APainter.paintLine(unit, runningManager.runTo(), Color.Green);
//                unit.setTooltip("RunAnyDir");
//            }

//            APainter.paintCircleFilled(runningManager.runTo(), 8, Color.Red);

            return true;
        }
        return false;
    }

    /**
     * Returns a place where run to, searching in all directions, which is walkable, inbounds and most distant
     * to given runAwayFrom position.
     */
    public APosition findRunPositionInAnyDirection(HasPosition runAwayFrom) {
        HasPosition runTo = runningManager.runTo();
        runAwayFrom = runAwayFrom.position();

        if (
            runTo != null
                && unit.distTo(runTo) > 1.5
                && unit.lastStartedRunningLessThanAgo(6)
        ) {
            return runTo.position();
        }

        int BASE_RADIUS = 4;
        int radius = BASE_RADIUS;
        APosition position = null;

        if (unit.enemiesNear().inRadius(8, unit).count() <= 1) {
            position = findPositionWithRadius(runAwayFrom, radius);
            if (position != null) return position;
        }

        radius = runAnyDirectionInitialRadius(unit, runAwayFrom);
        position = findPositionWithRadius(runAwayFrom, radius);
        if (position != null) return position;

        return null;
    }

    private APosition findPositionWithRadius(HasPosition runAwayFrom, int radius) {
        // Build list of possible run positions, basically around the clock
        ArrayList<APosition> potentialPositionsList = new ArrayList<>();
//        APainter.paintCircleFilled(enemyMedian, 8, Color.Purple); // @PAINT EnemyMedian

        positionSearchLoop(radius, potentialPositionsList);
//        positionSearchLoop((int) (radius * 0.6), potentialPositionsList);

        // =========================================================
        // Find the location that would be most distant to the enemy location
        double bestScore = -99;
        APosition bestPosition = null;
        for (APosition position : potentialPositionsList) {

            // Score is calculated as:
            // - being most distant to enemy we're running from,
            // - not close to ground friends,
            double positionScore = evalPosition(unit, position, runAwayFrom);

            boolean isNewBest = bestPosition == null || positionScore >= bestScore;
            if (isNewBest) {
                boolean targetPositionHasObstacles = unit.groundDist(position) >= 1.6 * unit.distTo(position);
//                System.out.println("unit.groundDist(position) = " + unit.groundDist(position));
//                System.out.println("unit.distTo(position) = " + unit.distTo(position));
//                if (targetPositionHasObstacles) System.out.println("targetPositionHasObstacles!!!");
//                System.out.println("--- ");
                if (targetPositionHasObstacles) continue;
                bestPosition = position;
                bestScore = positionScore;
            }
        }

//        System.out.println("### unit = " + unit.toStringPixels());
//        System.out.println("### runFrom = " + runAwayFrom.toStringPixels());
//        System.out.println("### bestPosition = " + bestPosition + " / " + bestPosition.toStringPixels());
//        System.out.println("### bestScore = " + bestScore);
//        System.out.println("### dist = " + unit.distTo(bestPosition));
//        System.out.println("-------------------------");

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
                APosition potentialPosition = unit.translateByTiles(dtx, dty);

                // If has path to given point, add it to the list of potential points
//                APainter.paintLine(unitPosition, potentialPosition, Color.Purple);
//                if (isPossibleAndReasonablePosition(unit, potentialPosition, false, "v", "x")) {
                if (
                    positionFinder.isPossibleAndReasonablePosition(
                        unit,
                        potentialPosition,
                        false,
                        null,
                        null
//                        "O",
//                        "x"
                    )
                        && potentialPosition.distTo(unit) >= 0.5
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
        else if (unit.isTerran() && unit.isInfantry()) {
            return A.inRange(2, (int) (unit.distTo(runFrom) * 2), ANY_DIRECTION_RADIUS_TERRAN_INFANTRY);
        }

        return ANY_DIRECTION_RADIUS_DEFAULT;
    }
}
