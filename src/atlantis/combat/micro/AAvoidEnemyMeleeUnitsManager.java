package atlantis.combat.micro;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;


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
            unit.setTooltip("Ruuuuuuun");
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
            if (unit.isType(AUnitType.Protoss_Reaver) && unit.getGroundWeaponCooldown() <= 4) {
                return true;
            }
        }

        // =========================================================

        boolean shouldSkip = unit.isAirUnit() || unit.isMeleeUnit();
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
        double baseCriticalDistance = (unit.isVulture() ? 3.4 : 2.8);
//        double baseCriticalDistance = 3.0;
        double healthBonus = (100 - unit.getHPPercent()) / 40;

//        double numberOfNearEnemiesBonus = Math.max(0.4,
//                ((Select.enemyRealUnits().inRadius(4, unit).count() - 1) / 12));
        double archonBonus = (((Select.enemy().ofType(AUnitType.Protoss_Archon)
                .inRadius(5, unit)).count() > 0) ? 1.2 : 0);

        double criticalDistance = baseCriticalDistance + healthBonus + archonBonus;
        double enemyDistance = nearestEnemy.distanceTo(unit);

        // isEnemyCriticallyClose
        if (enemyDistance < criticalDistance) {
//            APainter.paintCircle(unit.getPosition(), (int) (32 * criticalDistance), Color.Red);
//            APainter.paintLine(unit, nearestEnemy, Color.Red);
            return true;
        }
        else {
//            APainter.paintCircle(unit.getPosition(), (int) (32 * criticalDistance), Color.Green);
            return false;
        }
    }

//    private static boolean shouldInterruptPendingAttack(AUnit unit) {
////        if (!unit.isAttackFrame() && !unit.isStartingAttack()) {
////            return false;
////        }
//        AUnit nearestEnemy = unit.getCachedNearestMeleeEnemy();
//        double nearestEnemyDistance = nearestEnemy.distanceTo(unit);
//
////        APainter.paintTextCentered(unit, "" + enemyDistance, Color.Orange);
//
//        if (unit.isVulture()) {
////            return enemyDistance < 3.5 && unit.getHPPercent() < 40;
//            if (Select.enemyRealUnits().combatUnits().inRadius(2.5, unit).count() <= 1) {
//                if (nearestEnemyDistance < 1.7) {
//                    return true;
//                }
//            }
//            else {
//                if (nearestEnemyDistance < 2.5) {
//                    return true;
//                }
//            }
//
//            return false;
//        } else {
//            return nearestEnemyDistance < 1.8 && (unit.isAttackFrame() || unit.isStartingAttack()) && unit.getHPPercent() >= 30;
//        }
//    }

}
