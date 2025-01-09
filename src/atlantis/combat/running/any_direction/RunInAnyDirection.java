package atlantis.combat.running.any_direction;

import atlantis.combat.running.ARunningManager;
import atlantis.combat.running.IsReasonablePositionToRunTo;
import atlantis.combat.running.RunToPositionFinder;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

import java.util.ArrayList;

public class RunInAnyDirection {
    public static int ANY_DIRECTION_RADIUS_DEFAULT = 5;
    public static int ANY_DIRECTION_RADIUS_DRAGOON = 6;
    public static int ANY_DIRECTION_RADIUS_DRAGOON_IF_ENEMIES_CLOSE = 4;
    //    public static int ANY_DIRECTION_RADIUS_DRAGOON = 6;
//    public static int ANY_DIRECTION_RADIUS_DRAGOON_IF_ENEMIES_CLOSE = 4;
    public static int ANY_DIRECTION_RADIUS_TERRAN_INFANTRY = 5;
    public static int ANY_DIRECTION_RADIUS_VULTURE = 4;

    private AUnit unit = null;
    private ARunningManager runningManager;
    private RunToPositionFinder positionFinder;
    private HasPosition runAwayFrom;

    public RunInAnyDirection(RunToPositionFinder positionFinder) {
        this.positionFinder = positionFinder;
    }

    public HasPosition runInAnyDirection(HasPosition runAwayFrom) {
        this.runningManager = positionFinder.running();
        this.unit = runningManager.unit();
        this.runAwayFrom = runAwayFrom;

        // =========================================================

        runningManager.setRunTo(findRunPositionInAnyDirection(runAwayFrom, true));
        if (runningManager.runTo() == null) {
            runningManager.setRunTo(findRunPositionInAnyDirection(runAwayFrom, false));
        }

        // =============================================================================

//        if (runningManager.runTo() != null) {
//            Color color = Color.Purple;
//            APainter.paintLine(unit, runningManager.runTo(), color);
//            APainter.paintLine(
//                unit.translateByPixels(0, 1),
//                runningManager.runTo().translateByPixels(0, 1),
//                color
//            );
//        }

        if (handleInvalidCaseWhenRunToIsTooClose()) return runningManager.setRunTo(null);

        // =============================================================================

        return runningManager.runTo();
    }

    private static double evalPosition(AUnit unit, APosition position, HasPosition runAwayFrom, boolean considerOtherUnitsInGoToPlace) {
        Selection allUnitsToConsider = considerOtherUnitsInGoToPlace ? Select.all() : Select.mineralsAndGeysers();

        return 1.3 * position.distTo(runAwayFrom)
            - position.distTo(unit)
            - allUnitsToConsider.inRadius(0.4, position).exclude(unit).count() * 0.5;
    }

    private boolean handleInvalidCaseWhenRunToIsTooClose() {
        if (
            runningManager.runTo() != null
                && isDistTooClose(runningManager.runTo())
                && (wayTooShortRunTo() || stillInRangeOfRangedEnemy())
        ) {
            // Info: This is a known issue, I couldn't debug this, but it shouldn't be a huge problem...
            if (A.isUms()) {
                System.err.println("Invalid run_any_dir TOO_SHORT, dist = " + unit.distTo(runningManager.runTo()));
                AAdvancedPainter.paintCircleFilled(unit, 7, Color.Red);
            }
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

    private boolean isDistTooClose(HasPosition position) {
        return unit.distTo(position) < 0.5;
    }

    private boolean stillInRangeOfRangedEnemy() {
        Selection rangedEnemies = unit.enemiesNear().ranged();
        AUnit rangedEnemy = rangedEnemies.nearestTo(unit);
        if (rangedEnemy == null) return false;

        return rangedEnemies.inRadius(rangedEnemy.groundWeaponRange(), runningManager.runTo()).count() > 0;
    }

    private boolean wayTooShortRunTo() {
        return unit.distTo(runningManager.runTo().position()) <= 0.05;
    }

    /**
     * Returns a place where run to, searching in all directions, which is walkable, inbounds and most distant
     * to given runAwayFrom position.
     */
    public APosition findRunPositionInAnyDirection(HasPosition runAwayFrom, boolean considerOtherUnitsInGoToPlace) {
        HasPosition runTo = runningManager.runTo();
        runAwayFrom = runAwayFrom.position();

        if (
            runTo != null
                && isDistTooClose(runTo)
                && unit.lastStartedRunningLessThanAgo(12)
        ) {
            return runTo.position();
        }

        int radius;
        APosition position = null;

//        int BASE_RADIUS = 4;
//        int radius = runAnyDirectionInitialRadius(unit, runAwayFrom);
//        APosition position = null;
//
//        if (unit.enemiesNear().inRadius(8, unit).count() <= 1) {
//            position = findPositionWithRadius(runAwayFrom, radius, considerOtherUnitsInGoToPlace);
//            if (position != null) return position;
//        }

        radius = runAnyDirectionRadius(unit, runAwayFrom);
        position = findPositionWithRadius(runAwayFrom, radius, considerOtherUnitsInGoToPlace);
        if (position != null) return position;

        return null;
    }

    private APosition findPositionWithRadius(HasPosition runAwayFrom, int radius, boolean considerOtherUnitsInGoToPlace) {
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
            double positionScore = evalPosition(unit, position, runAwayFrom, considerOtherUnitsInGoToPlace);

//            if (Select.mineralsAndGeysers().countInRadius(unit.isNotLarge() ? 2 : 6, position) > 0) continue;

            boolean isNewBest = bestPosition == null || positionScore >= bestScore;
            if (isNewBest) {
                boolean targetPositionHasObstacles = unit.groundDist(position) >= 2.2 * unit.distTo(position);
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
                if (
                    IsReasonablePositionToRunTo.check(
                        unit, potentialPosition, runAwayFrom
//                        "O", "x"
                    )
                        && potentialPosition.distTo(unit) >= 0.4
                        && !potentialPosition.isCloseToMapBounds()
                ) {
                    potentialPositionsList.add(potentialPosition);
                }
            }
        }
    }

    int runAnyDirectionRadius(AUnit unit, HasPosition runFrom) {
        if (unit.isVulture()) {
            return ANY_DIRECTION_RADIUS_VULTURE;
        }
        else if (unit.isDragoon()) {
            return unit.nearestEnemyDist() <= 3.4
                ? ANY_DIRECTION_RADIUS_DRAGOON_IF_ENEMIES_CLOSE
                : ANY_DIRECTION_RADIUS_DRAGOON;
        }
        else if (unit.isTerran() && unit.isInfantry()) {
//            return A.inRange(2, (int) (unit.distTo(runFrom) * 2), ANY_DIRECTION_RADIUS_TERRAN_INFANTRY);
            return ANY_DIRECTION_RADIUS_TERRAN_INFANTRY;
        }

        return ANY_DIRECTION_RADIUS_DEFAULT;
    }
}
