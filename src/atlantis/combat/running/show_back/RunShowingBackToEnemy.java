package atlantis.combat.running.show_back;

import atlantis.combat.running.ARunningManager;
import atlantis.combat.running.IsReasonablePositionToRunTo;
import atlantis.combat.running.SeparateEarlyFromFriends;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.util.Vector;

public class RunShowingBackToEnemy {
    public static final double SHOW_BACK_DIST_DEFAULT = 4;
    public static final double SHOW_BACK_DIST_DRAGOON = 6;
    public static final double SHOW_BACK_DIST_TERRAN_INFANTRY = 5;
    public static final double SHOW_BACK_DIST_VULTURE = 5;

    private final ARunningManager running;
    private AUnit unit;

    // =========================================================

    public RunShowingBackToEnemy(atlantis.combat.running.ARunningManager ARunningManager) {
        this.running = ARunningManager;
    }

    // =========================================================

    public boolean shouldRunByShowingBackToEnemy() {
        this.unit = running.unit();

        return ShouldRunByShowingBackToEnemy.check(unit, running.runningFromUnit());
    }

    // =========================================================

    public boolean findPositionForShowingBackToEnemy(HasPosition runAwayFrom) {
        this.unit = running.unit();

        if (runAwayFrom instanceof AUnit) {
            APosition enemyPosition = runAwayFrom.position();
            if (enemyPosition != null) runAwayFrom = enemyPosition;
        }

        HasPosition runTo = running.runTo();

        if (
            runTo != null
                && unit.distTo(runTo) > 1
                && running.unit().lastStartedRunningLessThanAgo(8)
        ) {
            runTo = SeparateEarlyFromFriends.modifyPositionSlightly(runTo, runAwayFrom, running);

            if (IsReasonablePositionToRunTo.check(unit, runTo, runAwayFrom)) {
                running.setRunTo(runTo);
                return true;
            }
            else {
                runTo = null;
            }
        }

//        if (runTo == null || running.unit().lastStartedRunningLessThanAgo(8)) {
        if (runTo == null) {
            runTo = findRunPositionShowYourBackToEnemy(runAwayFrom);
        }

        running.setRunTo(runTo);

        if (runTo != null) {
//            APainter.paintCircleFilled(runTo, 3, Color.Brown);
//            APainter.paintLine(running.unit(), runTo, Color.Brown);
            running.unit().setTooltip("ShowBack");
        }

        return runTo != null;
    }


    /**
     * Simplest case: add enemy-to-you-vector to your own position.
     */
    private APosition findRunPositionShowYourBackToEnemy(HasPosition runAwayFrom) {
        APosition runTo = showBackToEnemyIfPossible(runAwayFrom, showBackRunPixelRadius(running.unit(), runAwayFrom));

        if (runTo != null && running.unit().distToMoreThan(runTo, 0.2)) {
            return runTo;
        }

//        runTo = showBackToEnemyIfPossible(runAwayFrom, 2.5);
//
//        if (runTo != null && running.unit().distToMoreThan(runTo, 0.002)) {
//            return runTo;
//        }

        return null;
    }

    private APosition showBackToEnemyIfPossible(HasPosition runAwayFrom, double runDistInPixels) {
//        if (running.unit().isGroundUnit()) return null;
        runAwayFrom = runAwayFrom.position();

        APosition runTo;
        runAwayFrom = runAwayFrom.position();
        double vectorLength = running.unit().distTo(runAwayFrom);

        if (vectorLength < 0.01) {
//            CameraCommander.centerCameraOn();
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

        runTo = runTo.makeBuildableGroundPositionFarFromBounds();
        if (runTo == null) return null;

        // If vector changed (meaning we almost reached map boundaries) disallow it
        if (runTo.getX() != oldX && runTo.getY() != oldY) {
//            throw new RuntimeException("aaa " + unit + " // " + unit.position() + " // " + runAwayFrom);
            return null;
        }

        // =========================================================

        // If run distance is acceptably long and it's connected, it's ok.
        if (IsReasonablePositionToRunTo.check(
            running.unit(), runTo, runAwayFrom
//            "O", "X"
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

    private double showBackRunPixelRadius(AUnit unit, HasPosition runAwayFrom) {
        if (unit.isFlying()) {
            return 1.1;
        }
        else if (unit.isTerranInfantry()) {
            return RunShowingBackToEnemy.SHOW_BACK_DIST_TERRAN_INFANTRY * 32;
        }
        else if (unit.isVulture()) {
            return RunShowingBackToEnemy.SHOW_BACK_DIST_VULTURE * 32;
        }
        else if (unit.isDragoon()) {
            return RunShowingBackToEnemy.SHOW_BACK_DIST_DRAGOON * 32;
        }

        return (RunShowingBackToEnemy.SHOW_BACK_DIST_DEFAULT * 32);
    }
}
