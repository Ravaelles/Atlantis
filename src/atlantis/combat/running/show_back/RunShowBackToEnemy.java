package atlantis.combat.running.show_back;

import atlantis.combat.running.ARunningManager;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.Vector;
import atlantis.util.log.ErrorLog;
import bwapi.Color;

public class RunShowBackToEnemy {
    public static final double SHOW_BACK_DIST_DEFAULT = 6;
    public static final double SHOW_BACK_DIST_DRAGOON = 3;
    public static final double SHOW_BACK_DIST_TERRAN_INFANTRY = 3;
    public static final double SHOW_BACK_DIST_VULTURE = 5;

    private final ARunningManager running;
    private AUnit unit;

    // =========================================================

    public RunShowBackToEnemy(atlantis.combat.running.ARunningManager ARunningManager) {
        this.running = ARunningManager;
    }

    // =========================================================

    public boolean shouldRunByShowingBackToEnemy() {
        this.unit = running.unit();

//        if (true) return false;
//        if (true) return true;

        if (unit.isFlying()) return true;

        if (unit.lastActionMoreThanAgo(30, Actions.RUN_IN_ANY_DIRECTION)) return false;
        if (unit.hp() <= 18 || unit.lastUnderAttackLessThanAgo(90)) return false;
        if (!unit.isWorker() && !unit.isDragoon()) return false;

        if (unit.isGroundUnit() && unit.meleeEnemiesNearCount(1.9) > 0) return true;

        return false;
    }

    // =========================================================

    public boolean positionForShowingBackToEnemy(HasPosition runAwayFrom) {
        this.unit = running.unit();

        if (runAwayFrom instanceof AUnit) {
            APosition enemyPosition = runAwayFrom.position();
            if (enemyPosition != null) runAwayFrom = enemyPosition;
        }

        if (running.runTo() == null || running.unit().lastActionMoreThanAgo(4)) {
            running.setRunTo(findRunPositionShowYourBackToEnemy(runAwayFrom));
        }

        if (running.runTo() != null) {
            APainter.paintCircleFilled(running.runTo(), 3, Color.Brown);
            APainter.paintLine(running.unit(), running.runTo(), Color.Brown);
            running.unit().setTooltip("ShowBack");
        }

        return false;
    }


    /**
     * Simplest case: add enemy-to-you-vector to your own position.
     */
    private APosition findRunPositionShowYourBackToEnemy(HasPosition runAwayFrom) {
        APosition runTo = showBackToEnemyIfPossible(runAwayFrom);

        if (runTo != null && running.unit().distToMoreThan(runTo, 0.002)) {
            return runTo;
        }

        return null;
    }

    private APosition showBackToEnemyIfPossible(HasPosition runAwayFrom) {
//        if (running.unit().isGroundUnit()) return null;

        APosition runTo;
        runAwayFrom = runAwayFrom.position();
        double vectorLength = running.unit().distTo(runAwayFrom);
        double runDistInPixels = showBackRunPixelRadius(running.unit(), runAwayFrom);

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

        runTo = runTo.makeValidFarFromBounds();

        // If vector changed (meaning we almost reached map boundaries) disallow it
        if (runTo.getX() != oldX && runTo.getY() != oldY) {
//            throw new RuntimeException("aaa " + unit + " // " + unit.position() + " // " + runAwayFrom);
            return null;
        }

        // =========================================================

        // If run distance is acceptably long and it's connected, it's ok.
        if (running.isPossibleAndReasonablePosition(
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

    private double showBackRunPixelRadius(AUnit unit, HasPosition runAwayFrom) {
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
}
