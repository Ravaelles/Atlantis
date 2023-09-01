package atlantis.combat.running;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

public class RunShowBackToEnemy {
    protected static final double SHOW_BACK_DIST_DEFAULT = 6;
    protected static final double SHOW_BACK_DIST_DRAGOON = 3;
    protected static final double SHOW_BACK_DIST_TERRAN_INFANTRY = 2;
    protected static final double SHOW_BACK_DIST_VULTURE = 5;

    private final ARunningManager runningManager;

    // =========================================================

    protected RunShowBackToEnemy(atlantis.combat.running.ARunningManager ARunningManager) {
        this.runningManager = ARunningManager;
    }

    // =========================================================

    protected boolean shouldRunByShowingBackToEnemy() {
//        if (true) return true;
//        if (true) return false;

        AUnit unit = runningManager.unit;

//        if (unit.isGroundUnit() && unit.meleeEnemiesNearCount(1.5) >= 0) return true;

        if (unit.isGroundUnit() && unit.meleeEnemiesNearCount(2.5) >= 0) return true;

//        if (
//            (
//                runningManager.unit.nearestEnemyDist() >= 2.3
////                ||
////                runningManager.unit.nearestEnemyDist() <= 1.7
//            )
//                && runningManager.unit.enemiesNearInRadius(3.9) <= 1
//        ) return true;

        return unit.isFlying();
    }

    /**
     * Simplest case: add enemy-to-you-vector to your own position.
     */
    HasPosition findRunPositionShowYourBackToEnemy(HasPosition runAwayFrom) {

        return runningManager.runPositionFinder.findRunPositionShowYourBackToEnemy(runAwayFrom);
    }

    HasPosition showBackToEnemyIfPossible(HasPosition runAwayFrom) {

        // Apply opposite 2D vector

        // === Ensure position is in bounds ========================================

        // If vector changed (meaning we almost reached map boundaries) disallow it

        // =========================================================

        // If run distance is acceptably long and it's connected, it's ok.
        return runningManager.runPositionFinder.showBackToEnemyIfPossible(runAwayFrom);
    }

    double showBackRunPixelRadius(AUnit unit, HasPosition runAwayFrom) {

        return runningManager.runPositionFinder.showBackRunPixelRadius(unit, runAwayFrom);
    }
}
