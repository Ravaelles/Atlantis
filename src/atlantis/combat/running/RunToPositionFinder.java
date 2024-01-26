package atlantis.combat.running;

import atlantis.combat.running.any_direction.RunInAnyDirection;
import atlantis.combat.running.show_back.RunShowBackToEnemy;
import atlantis.combat.running.to_building.ShouldRunTowardsBase;
import atlantis.combat.running.to_building.ShouldRunTowardsBunker;
import atlantis.debug.painter.APainter;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
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
    protected HasPosition findBestPositionToRun(HasPosition runAwayFrom, double dist, Action action) {
        AUnit unit = running.unit;

        if (!unit.isFlying() && !action.equals(Actions.MOVE_DANCE_AWAY)) {
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
