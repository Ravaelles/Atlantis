package atlantis.combat.running;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class ARunningManager {
    public static int STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO = 8;
    public static int STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO = 6;


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
        if (runFrom(runFrom, 0.5, Actions.RUN_ENEMY, true)) {
            unit.setTooltip(tooltip);
            return true;
        }
        return false;
    }

    //    public boolean runFrom(Object unitOrPosition, double dist) {
    public boolean runFrom(HasPosition runFrom, double dist, Action action, boolean allowedToNotifyNearUnitsToMakeSpace) {
        if (ShouldContinueRunning.handleContinueRunning(unit)) return true;

        // =========================================================

        if (runFrom instanceof AUnit) {
            unit.setRunningFrom((AUnit) runFrom);
        }

        this.runFrom = runFrom;
        this.allowedToNotifyNearUnitsToMakeSpace = allowedToNotifyNearUnitsToMakeSpace;
        verifyRunFromPosition(runFrom);

        // === Define run to position ==============================

        runTo = adjustRunFromPositionSlightlyToSeparateFromNearbyFriends(runFrom);
        runTo = runPositionFinder.findBestPositionToRun(runFrom, dist);

        // === Actual run order ====================================

        if (runTo != null && unit.distTo(runTo.position()) >= 0.05) {
            dist = unit.distTo(runTo);
            unit.setTooltip("RunToDist(" + String.format("%.1f", dist) + ")", false);
            return makeUnitRun(action);
        }

//        System.err.println(
//            "=== RUN ERROR ================= run:"
//                + (runTo != null ? runTo.toStringPixels() : "-")
//                + " / unit:" + unit.position().toStringPixels()
//        );

//        System.err.println("Unit position = " + unit.position() + " // " + unit);
//        System.err.println("runTo = " + runTo);
//        System.err.println("Our count = " + Select.ourWithUnfinished().exclude(unit).inRadius(unit.size(), unit).count());
//        System.err.println("Neutral count = " + Select.neutral().inRadius(unit.size(), unit).count());

        unit.addLog("CantRun");
        return false;
    }

    private void verifyRunFromPosition(HasPosition runAwayFrom) {
        if (runAwayFrom == null || runAwayFrom.position() == null) {
            System.err.println("Null unit to run from");
            stopRunning();
            throw new RuntimeException("Null unit to run from");
        }
    }

    private HasPosition adjustRunFromPositionSlightlyToSeparateFromNearbyFriends(HasPosition runAwayFrom) {
        Selection friendsVeryNear = unit.friendsNear().inRadius(1.2, unit);
        if (friendsVeryNear.size() == 1) {
            return runAwayFrom.translatePercentTowards(35, friendsVeryNear.first());
        }

        return runAwayFrom;
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
        if (unit == null) return false;

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
                if (allowedToNotifyNearUnitsToMakeSpace) {
                    (new NotifyNearUnitsToMakeSpaceToRun(unit)).notifyNearUnits();
                }

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

    public HasPosition setRunTo(HasPosition runTo) {
        this.runTo = runTo;
        return runTo;
    }
}
