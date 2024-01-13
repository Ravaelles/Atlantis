package atlantis.combat.running;

import atlantis.combat.running.any_direction.RunInAnyDirection;
import atlantis.combat.running.show_back.RunShowBackToEnemy;
import atlantis.combat.running.to_building.ShouldRunTowardsBase;
import atlantis.combat.running.to_building.ShouldRunTowardsBunker;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
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
    protected final RunShowBackToEnemy runShowBackToEnemy;

    public RunToPositionFinder(atlantis.combat.running.ARunningManager runningManager) {
        this.running = runningManager;
        this.runShowBackToEnemy = new RunShowBackToEnemy(runningManager);
    }

    /**
     * Running behavior which will make unit run straight away from the enemy.
     */
    protected HasPosition findBestPositionToRun(HasPosition runAwayFrom, double dist) {
        AUnit unit = running.unit;

        // Run to BUNKER
        if (ShouldRunTowardsBunker.check(unit, runAwayFrom)) {
            AUnit position = ShouldRunTowardsBunker.position();
            if (position != null) {
                unit.paintCircleFilled(8, Color.Teal);
                return running.setRunTo(position);
            }
        }

        // === Run directly to base ========================

        if (ShouldRunTowardsBase.check(unit, runAwayFrom)) {
            AUnit position = ShouldRunTowardsBase.position();
            if (position != null) {
                unit.paintCircleFilled(3, Color.Yellow);
                return running.setRunTo(position);
            }
        }

        // === Run directly away from the enemy ========================

        if (running.showBackToEnemy.shouldRunByShowingBackToEnemy()) {
            if (runShowBackToEnemy.positionForShowingBackToEnemy(runAwayFrom)) {
                Color color = Color.Blue;
                HasPosition runTo = unit.runningManager().runTo;
                APainter.paintLine(unit, runTo, color);
                APainter.paintLine(
                    unit.translateByPixels(0, 1),
                    runTo.translateByPixels(0, 1),
                    color
                );

                return running.runTo();
            }
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
//        int radius = runAnyDirectionInitialRadius();
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
        if (position == null) return false;

        if (unit.isFlying()) return true;

        position = position.makeWalkable(1);

        if (position == null) return false;


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

    // =========================================================

    public ARunningManager running() {
        return running;
    }
}
