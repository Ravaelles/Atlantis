package atlantis.combat.micro;

import atlantis.AGame;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.Color;


public class AAvoidEnemyMeleeUnitsManager {
    
    private static AUnit nearestEnemy = null;
//    private Select<AUnit> enemyRealUnitsSelector = null;
    
    // =========================================================

    /**
     * If unit is ranged unit like e.g. Marine, get away from very close melee units like e.g. Zealots.
     */
    public static boolean avoidCloseMeleeUnits(AUnit unit) {
        if (shouldNotAvoidMeleeUnits(unit)) {
            return false;
        }

        if (Select.enemyRealUnits().combatUnits().inRadius(6, unit).count() <= 0) {
            return false;
        }
        
        // =========================================================

        boolean isEnemyDangerouslyClose = AAvoidEnemyMeleeUnitsManager.shouldRunFromAnyEnemyMeleeUnit(unit);
        if (!isEnemyDangerouslyClose) {
            return false;
        }

        // === Run the fuck outta here ==============================
        
//            APainter.paintTextCentered(unit.getPosition().translateByPixels(0, -12), "RUN", Color.Red);

        if (unit.runFrom(null)) {
//                APainter.paintTextCentered(unit.getPosition().translateByPixels(0, -48), "RUUUUUUUN", Color.Orange);
            unit.setTooltip("MeleeRun");
            return true;
        }

        return handleErrorRun(unit);
    }

    // =========================================================

    private static boolean handleErrorRun(AUnit unit) {
        unit.setTooltip("ERROR_RUN");
        System.err.println("ERROR_RUN for " + unit.getShortNamePlusId());

        return AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit);
    }

    private static boolean shouldNotAvoidMeleeUnits(AUnit unit) {

        // === Issue orders every 3 frames or so ========================================
//        if (unit.getFramesSinceLastOrderWasIssued() <= 2 && !unit.isIdle()) {

            // Scout mustn't exit here, otherwise scouting behavior will override this behavior.
//            if (unit.isScout()) {
//                return true;
//            }
//        }

        // === Reaver should not avoid if has no cooldown ===============================

        if (AGame.isPlayingAsProtoss()) {
            if (
                Select.ourCombatUnits().inRadius(7, unit).count() >= 7
                && Select.ourCombatUnits().inRadius(4, unit).count() >= 3
            ) {
                return false;
            }

            if (unit.isType(AUnitType.Protoss_Reaver) && unit.getCooldownCurrent() <= 4) {
                return true;
            }
        }

        // =========================================================

        boolean shouldSkip = !unit.isWorker() && (unit.isAirUnit() || unit.isMeleeUnit());
        if (shouldSkip) {
            return true;
        }

        return false;
    }

    private static boolean shouldRunFromAnyEnemyMeleeUnit(AUnit unit) {
        Select<?> closeEnemies = Select.enemyRealUnits().combatUnits().melee().inRadius(6, unit);
        nearestEnemy = closeEnemies.nearestTo(unit);
        unit.setCachedNearestMeleeEnemy(nearestEnemy);

        if (nearestEnemy != null) {
            return isEnemyCriticallyClose(unit);
        }

        return false;
    }

    private static boolean isEnemyCriticallyClose(AUnit unit) {
        double criticalDistance = getCriticalDistance(unit);
        double enemyDistance = nearestEnemy.distanceTo(unit);

        // isEnemyCriticallyClose
        if (enemyDistance <= criticalDistance) {
//            APainter.paintCircle(unit.getPosition(), (int) (32 * criticalDistance), Color.Red);
            APainter.paintCircle(unit.getPosition(), 22, Color.Red);
            APainter.paintCircle(unit.getPosition(), 20, Color.Red);
            APainter.paintCircle(unit.getPosition(), 18, Color.Red);
//            APainter.paintLine(unit, nearestEnemy, Color.Red);
            return true;
        }
        else {
            APainter.paintCircle(unit.getPosition(), 20, Color.Green);
            APainter.paintCircle(unit.getPosition(), 17, Color.Green);
//            APainter.paintCircle(unit.getPosition(), (int) (32 * criticalDistance), Color.Green);
            return false;
        }
    }

    public static double getCriticalDistance(AUnit unit) {
        if (nearestEnemy == null) {
            return -Double.POSITIVE_INFINITY;
        }

        double quicknessDifference = unit.getSpeed() - nearestEnemy.getSpeed();
//        System.out.println("quicknessDifference = " + quicknessDifference + " ### " + unit.getSpeed() + " // " + nearestEnemy.getSpeed());

        // If unit is very slow, don't run at all
        if (quicknessDifference <= -0.3) {
            return -Double.POSITIVE_INFINITY;
        }

//        double baseCriticalDistance = (unit.isQuick() ? 4.8 : 3.0);
        double baseCriticalDistance = 3.0;
        double quicknessBonus = Math.min(quicknessDifference * 2, 1.7);
        double healthBonus = unit.getWoundPercent() / 38;
        double archonBonus = (((Select.enemy().ofType(AUnitType.Protoss_Archon).inRadius(5, unit)).count() > 0) ? 1.2 : 0);
//        double movementBonus = unit.isMoving() ? (unit.lastRunAgo(12) ? -1.3 : 1.2) : 0;
        double movementBonus = unit.isMoving() ? (unit.lastStartedRunningAgo(12) ? -1.3 : 1.2) : 0;
        double directionBonus = nearestEnemy != null && unit.isOtherUnitFacingThisUnit(nearestEnemy) ? -2.5 : 2;

        double criticalDistance = baseCriticalDistance + quicknessBonus + healthBonus + archonBonus + movementBonus + directionBonus;

        return criticalDistance;
    }

}
