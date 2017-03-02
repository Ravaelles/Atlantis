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
        
        boolean isAllowedType = (unit.isGroundUnit() && unit.getType().isRangedUnit()) || unit.isWorker();
        boolean isHealthyAndHasManyHP = unit.getHitPoints() >= 60 && unit.getHPPercent() >= 100;
        
        Select<AUnit> enemyRealUnitsSelector = Select.enemyRealUnits().combatUnits();
        
//        APainter.paintTextCentered(unit, "" + unit.getID(), Color.Black);
        
        if (isAllowedType && (!isHealthyAndHasManyHP || unit.isVulture())
                && enemyRealUnitsSelector.inRadius(5, unit).count() > 0) {
            
            // === Define safety distance ==============================

            double lowHealthBonus = Math.max(((100 - unit.getHPPercent()) / 25), 1.7);
            double safetyDistance;
            
            if (unit.isVulture()) {
                safetyDistance = 3 + lowHealthBonus;
            }
            else if (unit.isWorker()) {
                if (unit.isGatheringGas() || unit.isGatheringMinerals()) {
                    safetyDistance = 0.8 + lowHealthBonus;
                }
                else {
                    safetyDistance = 1.4 + lowHealthBonus;
                }
            }
            else {
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
                }
                else {
                    safetyDistance += Math.max((double) enemiesNearby / 3, 2);
                }
            }
            
//            APainter.paintTextCentered(unit.getPosition().translateByPixels(0, -12), 
//                    "" + String.format("%.1f", safetyDistance), Color.Green);
            
//            AtlantisPainter.paintCircle(unit, (int) safetyDistance * 32, Color.Green);
//            APainter.paintTextCentered(unit.getPosition().translateByPixels(0, -12), enemiesNearby + "", Color.Red);

            Select<?> closeEnemies = enemyRealUnitsSelector.melee().inRadius(safetyDistance, unit);
            AUnit closeEnemy = closeEnemies.nearestTo(unit);
//            APainter.paintCircleFilled(unit.getPosition(), 11, Color.White);
            if (closeEnemy != null) {
                
                double baseCriticalDistance = (unit.isVulture() ? 2 : 1.5);
                double healthBonus = unit.getHPPercent() < 30 ? 1.8 : 0;
                double numberOfNearEnemiesBonus = Math.max(0.4, 
                        ((Select.enemyRealUnits().inRadius(4, unit).count() - 1) / 12));
                double archonBonus = (((enemyRealUnitsSelector.ofType(AUnitType.Protoss_Archon)
                        .inRadius(5, unit)).count() > 0) ? 2.2 : 0);
                
                double dangerousDistance = baseCriticalDistance + numberOfNearEnemiesBonus 
                        + healthBonus + archonBonus;
                double enemyDistance = closeEnemy.distanceTo(unit);
                boolean isEnemyDangerouslyClose = enemyDistance < dangerousDistance;
//                APainter.paintCircleFilled(unit.getPosition(), 11, Color.Yellow);
                
                boolean standardEnemyIsDangerouslyClose = 
//                        isEnemyDangerouslyClose && (unit.type().isMechanical() || unit.isWounded());
                        isEnemyDangerouslyClose;
                if (standardEnemyIsDangerouslyClose) {
                    
//                    APainter.paintCircleFilled(unit.getPosition(), 11, Color.Blue);
                    
                    // === Don't INTERRUPT shooting ============================

                    boolean dontInterruptPendingAttack = false;
                    if (unit.isVulture()) {
                        dontInterruptPendingAttack = unit.isAttackFrame() && unit.getHPPercent() >= 40;
                    }
                    else {
                        dontInterruptPendingAttack = (unit.isAttackFrame() || unit.isStartingAttack())
                                && unit.getHPPercent() >= 30;
                    }
                    
                    // =========================================================
                    
                    // Don't run, cause unit is SHOOTING
                    if (dontInterruptPendingAttack) {
                        unit.setTooltip("Shooting" + (unit.getTarget() != null 
                                ? " " + unit.getTarget().getShortName() : ""));
                    }
                    
                    // RUN
                    else {
//                        APainter.paintTextCentered(unit.getPosition().translateByPixels(0, -12), "RUN", Color.Red);
                        if (unit.runFrom(null)) {
    //                        AtlantisPainter.paintCircle(unit, (int) safetyDistance * 32, Color.Red);
    //                        AtlantisPainter.paintCircle(unit, enemyNearbyCountingRadius * 32, Color.Red);
    //                        unit.setTooltip("Melee-run " + closeEnemy.getShortName());
//                            unit.setTooltip("Melee-run (" + closeEnemy.getShortName() + ")");
                            unit.setTooltip("Melee-run (" + String.format("%.1f", closeEnemy.distanceTo(unit)) + ")");
                            return true;
                        }
                        else {
                            unit.setTooltip("Unable to run");
                            return false;
                        }
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
            if (!AScoutManager.isScout(unit)) {
                return true;
            }
        }
        
        // === Reaver should not avoid if has no cooldown ===============================
        
        if (AGame.playsAsProtoss()) {
            if (unit.isType(AUnitType.Protoss_Reaver) && unit.getGroundWeaponCooldown() <= 0) {
                return true;
            }
        }
        
        // ==============================================================================
        
        return false;
    }
    
}
