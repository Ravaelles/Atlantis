package atlantis.combat.running;

import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;
import bwapi.Color;

public class ARunningManager {

    //    public static double MIN_DIST_TO_REGION_BOUNDARY = 1;
    public static int STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO = 8;
    public static int STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO = 6;
    public static double NOTIFY_UNITS_MAKE_SPACE = 0.75;
    public static double NOTIFY_UNITS_IN_RADIUS = 0.2;


    protected final AUnit unit;
    protected HasPosition runTo = null;
    protected HasPosition runFrom = null;
    protected boolean allowedToNotifyNearUnitsToMakeSpace;

    protected final RunShowBackToEnemy showBackToEnemy = new RunShowBackToEnemy(this);
    protected final RunTowardsNonStandard runTowardsNonStandard = new RunTowardsNonStandard(this);
    protected final ReasonableRunToPosition reasonableRunToPosition = new ReasonableRunToPosition(this);
    protected final RunToPositionFinder runPositionFinder = new RunToPositionFinder(this);

    // =========================================================

    public ARunningManager(AUnit unit) {
        this.unit = unit;
    }

    // =========================================================

    public boolean runFromAndNotifyOthersToMove(HasPosition runFrom, String tooltip) {
        if (runFrom(runFrom, 1.0, Actions.RUN_ENEMY, true)) {
            unit.setTooltip(tooltip);
            return true;
        }
        return false;
    }

    //    public boolean runFrom(Object unitOrPosition, double dist) {
    public boolean runFrom(HasPosition runFrom, double dist, Action action, boolean allowedToNotifyNearUnitsToMakeSpace) {
        this.runFrom = runFrom;
        this.allowedToNotifyNearUnitsToMakeSpace = allowedToNotifyNearUnitsToMakeSpace;

        handleInvalidRunFromPosition(runFrom);

//        if (handleOnlyCombatBuildingsAreDangerouslyClose()) return true;
        if (handleContinueRunning()) return true;

        // === Define run to position ==============================

        runTo = adjustRunFromPositionSlightlyToSeparateFromNearbyFriends(runFrom);
        runTo = runPositionFinder.findBestPositionToRun(runFrom, dist);

        // === Actual run order ====================================

        if (runTo != null && unit.distTo(runTo) >= 0.001) {
            dist = unit.distTo(runTo);
            unit.setTooltip("RunToDist(" + String.format("%.1f", dist) + ")", false);
            return makeUnitRun(action);
        }

//        System.err.println("=== RUN ERROR =================");
//        System.err.println("Unit position = " + unit.position() + " // " + unit);
//        System.err.println("runTo = " + runTo);
//        System.err.println("Our count = " + Select.ourWithUnfinished().exclude(unit).inRadius(unit.size(), unit).count());
//        System.err.println("Neutral count = " + Select.neutral().inRadius(unit.size(), unit).count());

        unit.setTooltip("Cant run", false);
        return false;
    }

    private boolean handleContinueRunning() {
        if (unit.isRunning()) {
            if (unit.lastStartedRunningLessThanAgo(5)) {
//                System.out.println("@ " + A.now() + " - StartedRunning " + unit.idWithHash() + " / " + unit.lastStartedRunningAgo());
                return true;
            }

            if (
                unit.isMoving() && unit.lastActionLessThanAgo(15, Actions.RUN_IN_ANY_DIRECTION)
                || unit.lastActionLessThanAgo(8, Actions.RUN_IN_ANY_DIRECTION)
            ) {
//                System.err.println("@ " + A.now() + " - " + unit);
                return true;
            }
        }

        return false;
    }

    private void handleInvalidRunFromPosition(HasPosition runAwayFrom) {
        if (runAwayFrom == null || runAwayFrom.position() == null) {
            System.err.println("Null unit to run from");
            stopRunning();
            throw new RuntimeException("Null unit to run from");
        }
    }

    private HasPosition adjustRunFromPositionSlightlyToSeparateFromNearbyFriends(HasPosition runAwayFrom) {
        Selection friendsVeryNear = unit.friendsNear().inRadius(1.2, unit);
        if (friendsVeryNear.size() == 1) {
//            System.out.println("runAwayFrom = " + runAwayFrom);
//            System.out.println("and now = " + runAwayFrom.translatePercentTowards(20, friendsVeryNear.first()));
            return runAwayFrom.translatePercentTowards(35, friendsVeryNear.first());
        }

        return runAwayFrom;
    }

    // =========================================================

    /**
     * Tell other units that might be blocking our escape route to move.
     */
    private boolean notifyNearUnitsToMakeSpace() {
        if (!allowedToNotifyNearUnitsToMakeSpace) {
            return false;
        }

        if (We.protoss() && unit.friendsNear().inRadius(0.3, unit).atMost(1)) {
            return false;
        }

        if (unit.isFlying() || unit.isLoaded()) {
            return false;
        }

//        if (unit.enemiesNear().melee().inRadius(4, unit).empty()) {
//            return false;
//        }

        Selection friendsTooClose = Select.ourRealUnits()
            .exclude(unit)
            .groundUnits()
            .inRadius(NOTIFY_UNITS_IN_RADIUS, unit);

        if (friendsTooClose.count() <= 1) {
            return false;
        }

        for (AUnit otherUnit : friendsTooClose.list()) {
            if (canBeNotifiedToMakeSpace(otherUnit)) {
                AUnit runFrom = otherUnit.enemiesNear().nearestTo(otherUnit);
                if (runFrom == null || !runFrom.hasPosition()) {
                    continue;
                }

//                System.err.println(otherUnit + " // notified by " + unit + " (" + unit.hp() + ")");

                otherUnit.runningManager().runFrom(runFrom, NOTIFY_UNITS_MAKE_SPACE, Actions.MOVE_SPACE, true);
                APainter.paintCircleFilled(unit, 10, Color.Yellow);
                APainter.paintCircleFilled(otherUnit, 7, Color.Grey);
                otherUnit.setTooltip("MakeSpace" + A.dist(otherUnit, unit), false);
            }
        }
        return true;
    }

    private boolean canBeNotifiedToMakeSpace(AUnit unit) {
        return !unit.isRunning() && !unit.type().isReaver() && unit.lastStartedRunningMoreThanAgo(3);
    }

    // =========================================================

    /**
     * Returns true if given run position is traversable, land-connected and not very, very far
     */
    public boolean isPossibleAndReasonablePosition(
        AUnit unit, APosition position
    ) {
        return reasonableRunToPosition.isPossibleAndReasonablePosition(unit, position);
    }

    public boolean isPossibleAndReasonablePosition(
        AUnit unit, APosition position, boolean includeNearWalkability, String charForIsOk, String charForNotOk
    ) {
        return reasonableRunToPosition.isPossibleAndReasonablePosition(unit, position, includeNearWalkability, charForIsOk, charForNotOk);
    }

//    private boolean handleOnlyCombatBuildingsAreDangerouslyClose(AUnit unit) {
//        if (unit.isRunning()) {
//            return false;
//        }
//
//        // Check if only combat buildings are dangerously close. If so, don't run in any direction.
//        Units dangerous = AvoidEnemies.unitsToAvoid(unit, true);
//
//        if (dangerous.isEmpty()) {
//            return false;
//        }
//
//        Selection combatBuildings = Select.from(dangerous).combatBuildings(false);
//        if (dangerous.size() == combatBuildings.size() && unit.enemiesNear().combatUnits().atMost(1)) {
//            double minDist = unit.isGhost() ? 9.5 : 7.5;
//            AUnit combatBuilding = combatBuildings.nearestTo(unit);
//
//            if (combatBuilding.distToLessThan(unit, minDist)) {
//                if (unit.isHoldingPosition() && unit.lastActionMoreThanAgo(30)) {
//                    if (unit.moveAwayFrom(combatBuilding, 0.3, "Careful", Actions.RUN_ENEMY)) {
//                        return true;
//                    }
//                }
//                else if (unit.isMoving() && unit.isAction(Actions.RUN_ENEMY)) {
//                    unit.holdPosition("Steady");
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }

//    private boolean distToNearestRegionBoundaryIsOkay(APosition position) {
//        ARegion region = position.region();
//        if (region != null) {
//            return true;
//        }
//
//        for (ARegionBoundary boundary : region.boundaries()) {
//            if (boundary.distToLessThan(unit, MIN_DIST_TO_REGION_BOUNDARY)) {
//                return false;
//            }
//        }
//
//        return true;
//    }

    private boolean makeUnitRun(Action action) {
        if (unit == null) {
            return false;
        }

        if (runTo == null) {
            stopRunning();
            System.err.println("RunTo should not be null!");
            unit.setTooltip("Fuck!", false);
            return true;
        }

        // === Valid run position ==============================

        else {
            // Update last time run order was issued
            unit._lastStartedRunning = A.now();

            if (unit.move(runTo, action, "Run(" + A.digit(unit.distTo(runTo)) + ")", false)) {
                // Make all other units very close to it run as well
                notifyNearUnitsToMakeSpace();

                return true;
            }
            return false;
        }

    }

    // === Getters ========================================

    public HasPosition runToPosition() {
        return runTo;
    }

    public boolean isRunning() {
        if (runTo != null && unit.distTo(runTo) >= 0.08) {
            return true;
//            if (unit.lastStartedRunningAgo(3)) {
//                return true;
//            } else {
//                stopRunning();
//                return false;
//            }
        }

//        stopRunning();
        return false;
    }

    public void stopRunning() {
        runTo = null;
        unit._lastStoppedRunning = A.now();
    }

    public AUnit unit() {
        return unit;
    }

    public HasPosition runTo() {
        return runTo;
    }

    protected HasPosition setRunTo(HasPosition runTo) {
        this.runTo = runTo;
        return runTo;
    }
}
