package atlantis.combat.running;

import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.running.fallback.RunAttackFallback;
import atlantis.combat.running.show_back.RunShowingBackToEnemy;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.log.ErrorLog;
import bwapi.Color;

public class ARunningManager {
//    public static int STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO = 8;
//    public static int STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO = 6;

    protected final AUnit unit;
    protected HasPosition runTo = null;
    protected AUnit runningFromUnit = null;
    protected HasPosition runningFromPosition = null;
    protected boolean allowedToNotifyNearUnitsToMakeSpace;
    protected String _lastRunMode = "Init";

    protected final RunShowingBackToEnemy showBackToEnemy = new RunShowingBackToEnemy(this);
    //    protected final RunTowardsNonStandard runTowardsNonStandard = new RunTowardsNonStandard(this);
    protected final RunToPositionFinder runPositionFinder = new RunToPositionFinder(this);

    // =========================================================

    public ARunningManager(AUnit unit) {
        this.unit = unit;
    }

    // =========================================================

    public boolean runFromAndNotifyOthersToMove(HasPosition runFrom, String tooltip) {
        if (runFrom(runFrom, 1, Actions.RUN_ENEMY, true)) {
            unit.setTooltip(tooltip);
            return true;
        }
        return false;
    }

    //    public boolean runFrom(Object unitOrPosition, double dist) {
    public boolean runFrom(HasPosition runFrom, double dist, Action action, boolean allowedToNotifyNearUnitsToMakeSpace) {
        if (unit.lastStartedRunningLessThanAgo(1)) return true;

//        _lastRunMode = "Undefined";

        if (runFrom instanceof AUnit) {
            runningFromUnit = (AUnit) runFrom;
            runningFromPosition = runFrom;
        }
        else {
            runningFromUnit = null;
            runningFromPosition = runFrom;
        }

        this.allowedToNotifyNearUnitsToMakeSpace = allowedToNotifyNearUnitsToMakeSpace;
        verifyRunFromPosition(runFrom);

        // === Define run to position ==============================

        runFrom = adjustRunFromPositionSlightlyToSeparateFromNearbyFriends(runFrom);
        runTo = runPositionFinder.findBestPositionToRun(runFrom, dist, action);

        // === Actual run order ====================================

        if (validateAndRun(action, 1, false)) return makeUnitRun(action);

        if (A.isUms() && !unit.isObserver()) {
//            System.err.println(
//                "=== RUN ERROR ================= run:"
//                    + (runTo != null ? runTo.toStringPixels() : "-")
//                    + " / unit:" + unit.position().toStringPixels()
//                    + " / dist:" + (runTo != null ? runTo.distToDigit(unit) : "-")
//                    + " / walkable:" + (runTo != null && runTo.isWalkable() ? "Y" : "N")
//                    + " / method:" + method
//            );
//            CameraCommander.centerCameraOn(unit);
            unit.paintCircleFilled(8, Color.Red);
//            PauseAndCenter.on(unit);
        }

        if (validateAndRun(action, 1, true)) return makeUnitRun(action);

//        System.err.println("Unit position = " + unit.position() + " // " + unit);
//        System.err.println("runTo = " + runTo);
//        System.err.println("Our count = " + Select.ourWithUnfinished().exclude(unit).inRadius(unit.size(), unit).count());
//        System.err.println("Neutral count = " + Select.neutral().inRadius(unit.size(), unit).count());

        return actWhenCantRun();
    }

    private boolean validateAndRun(Action action, double minDist, boolean prioritizeMoving) {
        if (
            runTo != null
                && runTo.isWalkable()
                && unit.distTo(runTo.position()) >= 0.05
                && ((prioritizeMoving && unit.isMoving()) || unit.distTo(runTo.position()) >= minDist)
        ) {
            double dist = unit.distTo(runTo);
            unit.setTooltip("RunToDist(" + String.format("%.1f", dist) + ")", false);
            return true;
        }

//        System.err.println("runTo.isWalkable() = " + runTo.isWalkable());
//        System.err.println("unit.distTo(runTo.position()) = " + unit.distTo(runTo.position()));

        return false;
    }

    private boolean actWhenCantRun() {
        unit.addLog("CantRun");

        if (unit.hp() >= 80) {
            if ((new AttackNearbyEnemies(unit)).forceHandle() != null) {
                unit.setTooltip("CantRun-Attack");
                unit.setManagerUsed(new RunAttackFallback(unit));
                return true;
            }
        }

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
        Selection friendsVeryNear = unit.friendsNear().inRadius(1.4, unit);
        if (friendsVeryNear.size() == 1) {
            return runAwayFrom.translatePercentTowards(20, friendsVeryNear.first());
        }

        return runAwayFrom;
    }

    // =========================================================

//    public boolean isReasonablePositionToRun(AUnit unit, APosition position) {
//        return IsReasonablePositionToRunTo.isPossibleAndReasonablePosition(
//            unit, position, runningFromPosition, true, null, null
//        );
//    }

    private boolean makeUnitRun(Action action) {
        if (unit == null) {
            ErrorLog.printMaxOncePerMinute("Unit is null in makeUnitRun");
            return false;
        }

        if (runTo == null) {
            stopRunning();
            ErrorLog.printMaxOncePerMinute("RunTo should not be null!");
            unit.setTooltip("Fuck!", false);
            return true;
        }

        // === Valid run position ==============================

        else {
            if (unit.move(runTo, action, "Run(" + A.digit(unit.distTo(runTo)) + ")", false)) {
                // Update last time run order was issued
                if (unit._lastStartedRunning <= unit._lastStoppedRunning) unit._lastStartedRunning = A.now();

                // Make all other units very close to it run as well
                if (allowedToNotifyNearUnitsToMakeSpace) {
                    (new NotifyNearUnitsToMakeSpaceToRun(unit)).notifyNearUnits(this.runningFromPosition());
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
        if (runTo != null && unit.distTo(runTo) >= 0.04) {
            return true;
//            if (unit.lastStartedRunningAgo(3)) {
//                return true;
//            } else {
//                stopRunning();
//                return false;
//            }
        }

//        stopRunning();
        return unit.isMoving()
            && unit().action().isRunning()
            && unit.distTo(unit.targetPosition()) >= 0.04;
//        return false;
    }

    public void stopRunning() {
        if (unit.isRunning()) {
            unit._lastStoppedRunning = A.now();
//            System.out.println("Stopped running at " + A.now());
//            A.printStackTrace("StoppedRunning at " + A.now());
        }

        runTo = null;
        runningFromPosition = null;
        runningFromUnit = null;

        if (unit.isMoving() && unit.lastActionMoreThanAgo(1)) unit.holdToShoot();

//        A.printStackTrace("StoppedRunning");
    }

    public AUnit unit() {
        return unit;
    }

    public HasPosition runTo() {
        return runTo;
    }

    public AUnit runningFromUnit() {
        return runningFromUnit;
    }

    public HasPosition runningFromPosition() {
        return runningFromPosition;
    }

//    public HasPosition setRunFromUnit(HasPosition runFrom) {
//        this.runFrom = runFrom;
//        return runFrom;
//    }
//
//    public HasPosition setRunFrom(HasPosition runFrom) {
//        this.runFrom = runFrom;
//        return runFrom;
//    }

    public HasPosition setRunTo(HasPosition runTo) {
        this.runTo = runTo;
        unit._lastRunningPositionChange = A.now;
        return runTo;
    }

    public String lastRunMode() {
        return _lastRunMode;
    }
}
