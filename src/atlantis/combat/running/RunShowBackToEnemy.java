package atlantis.combat.running;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

public class RunShowBackToEnemy {
    protected static final double SHOW_BACK_DIST_DEFAULT = 6;
    protected static final double SHOW_BACK_DIST_DRAGOON = 3;
    protected static final double SHOW_BACK_DIST_TERRAN_INFANTRY = 3;
    protected static final double SHOW_BACK_DIST_VULTURE = 5;

    private final ARunningManager runningManager;

    // =========================================================

    protected RunShowBackToEnemy(atlantis.combat.running.ARunningManager ARunningManager) {
        this.runningManager = ARunningManager;
    }

    // =========================================================

    protected boolean shouldRunByShowingBackToEnemy() {
        if (true) return false;
//        if (true) return true;

        AUnit unit = runningManager.unit;

//        if (unit.hp() <= 18) return false;

//        if (unit.isGroundUnit() && unit.meleeEnemiesNearCount(1.5) >= 0) return true;

        if (
            unit.isGroundUnit()
                && unit.meleeEnemiesNearCount(1.6) >= 0
        ) return true;

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
}
