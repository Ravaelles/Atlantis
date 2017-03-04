package atlantis.combat.micro;

import atlantis.AGame;
import atlantis.debug.APainter;
import atlantis.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.Color;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AAvoidMeleeUnitsManager {
    
    /**
     * If unit is ranged unit like e.g. Marine, get away from very close melee units like e.g. Zealots.
     */
    public static boolean avoidCloseMeleeUnits(AUnit unit) {
        if (shouldSkip(unit)) {
            return true;
        }
        
        // =========================================================

        Select<AUnit> enemyRealUnitsSelector = Select.enemyRealUnits().combatUnits();
        if (enemyRealUnitsSelector.inRadius(5, unit).count() > 0) {

            // === Define safety distance ==================================
            
            boolean isEnemyDangerouslyClose = isEnemyDangerouslyClose(unit, enemyRealUnitsSelector);
            if (isEnemyDangerouslyClose) {
                boolean canInterruptPendingAttack = canInterruptPendingAttack(unit);

                // =========================================================
                // Don't run, cause unit is SHOOTING
                if (!canInterruptPendingAttack) {
                    unit.setTooltip("Shoot " + (unit.getTarget() != null
                            ? " " + unit.getTarget().getShortName() : ""));
                    return true;
                } 

                // === Run the hella outta here ============================
                else {
                    APainter.paintTextCentered(unit.getPosition().translateByPixels(0, -12), "RUN", Color.Red);
                    if (unit.runFrom(null)) {
                        unit.setTooltip("Melee-run");
                        return true;
                    } else {
                        unit.setTooltip("ERROR_RUN");
                        return false;
                    }
                }
            }
        }
        
        return false;
    }

    // =========================================================
    
    private static boolean shouldSkip(AUnit unit) {

        // === Issue orders every 3 frames or so ========================================
        if (unit.getFramesSinceLastOrderWasIssued() <= 2 && !unit.isIdle()) {

            // Scout mustn't exit here, otherwise scouting behavior will override this behavior.
            if (!unit.isScout()) {
                return true;
            }
        }

        // === Reaver should not avoid if has no cooldown ===============================
        if (AGame.playsAsProtoss()) {
            if (unit.isType(AUnitType.Protoss_Reaver) && unit.getGroundWeaponCooldown() <= 0) {
                return true;
            }
        }
        
        // =========================================================
        
        boolean isAllowedType = (unit.isGroundUnit() && unit.getType().isRangedUnit()) || unit.isWorker();
//        boolean isHealthyAndHasManyHP = unit.getHitPoints() >= 60 && unit.getHPPercent() >= 100;
        if (!isAllowedType) {
            return true;
        }

        // =========================================================
                 
        return false;
    }

    private static boolean isEnemyDangerouslyClose(AUnit unit, Select<AUnit> enemyRealUnitsSelector) {
        double lowHealthBonus = Math.max(((100 - unit.getHPPercent()) / 25), 1.7);
        double safetyDistance;

        if (unit.isVulture()) {
            safetyDistance = 4 + lowHealthBonus;
        } else if (unit.isWorker()) {
            if (unit.isGatheringGas() || unit.isGatheringMinerals()) {
                safetyDistance = 0.8 + lowHealthBonus;
            } else {
                safetyDistance = 1.4 + lowHealthBonus;
            }
        } else {
            safetyDistance = 2.2 + lowHealthBonus;

            if (unit.getWeaponRangeGround() > 1 && safetyDistance > unit.getWeaponRangeGround()) {
                safetyDistance = unit.getWeaponRangeGround() - 0.15;
            }
        }

        // =========================================================
        // Apply bonus when there are maaany enemies nearby
//            int enemyNearbyCountingRadius = 7;
//            int enemiesNearby = Select.enemy().inRadius(enemyNearbyCountingRadius, unit).count();
        int enemiesNearby = enemyRealUnitsSelector.inRadius(safetyDistance, unit).count();
        if (enemiesNearby >= 2) {
            if (unit.isVulture()) {
                safetyDistance += Math.max((double) enemiesNearby / 4, 3.5);
            } else {
                safetyDistance += Math.max((double) enemiesNearby / 3, 2);
            }
        }

//            APainter.paintTextCentered(unit.getPosition().translateByPixels(0, -12), 
//                    "" + String.format("%.1f", safetyDistance), Color.Green);
//            APainter.paintCircle(unit, (int) safetyDistance * 32, Color.Green);
//            APainter.paintTextCentered(unit.getPosition().translateByPixels(0, -12), enemiesNearby + "", Color.Red);
        Select<?> closeEnemies = enemyRealUnitsSelector.melee().inRadius(safetyDistance, unit);
        AUnit nearestEnemy = closeEnemies.nearestTo(unit);
        unit.setCachedNearestMeleeEnemy(nearestEnemy);
        
//            APainter.paintCircleFilled(unit.getPosition(), 11, Color.White);
        if (nearestEnemy != null) {

            double baseCriticalDistance = (unit.isVulture() ? 3.45 : 2.2);
            double healthBonus = unit.getHPPercent() < 30 ? 0.5 : 0;
            double numberOfNearEnemiesBonus = Math.max(0.4,
                    ((Select.enemyRealUnits().inRadius(4, unit).count() - 1) / 12));
            double archonBonus = (((enemyRealUnitsSelector.ofType(AUnitType.Protoss_Archon)
                    .inRadius(5, unit)).count() > 0) ? 1 : 0);

            double dangerousDistance = baseCriticalDistance + numberOfNearEnemiesBonus
                    + healthBonus + archonBonus;
            double enemyDistance = nearestEnemy.distanceTo(unit);
            boolean isEnemyDangerouslyClose = enemyDistance < dangerousDistance;

            if (isEnemyDangerouslyClose) {
//                APainter.paintCircle(unit.getPosition(), (int) (32 * enemyDistance), Color.Red);
                APainter.paintLine(unit, nearestEnemy, Color.Red);
            }
//                APainter.paintCircle(unit.getPosition(), (int) (32 * dangerousDistance), Color.Green);

            return isEnemyDangerouslyClose;
        }
        
        return false;
    }

    private static boolean canInterruptPendingAttack(AUnit unit) {
        AUnit nearestEnemy = unit.getCachedNearestMeleeEnemy();
        double enemyDistance = nearestEnemy.distanceTo(unit);
        
        if (unit.isVulture()) {
            return enemyDistance < 3.5 && unit.getHPPercent() < 40;
        } else {
//            return enemyDistance < 1.8
//                    && (unit.isAttackFrame() || unit.isStartingAttack()) && unit.getHPPercent() >= 30;
            return enemyDistance < 1.8
                    && (unit.isAttackFrame() || unit.isStartingAttack()) && unit.getHPPercent() <= 30;
        }
    }

}
