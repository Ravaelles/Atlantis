package atlantis.combat.micro;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisAvoidMeleeUnitsManager {
    
    /**
     * If unit is ranged unit like e.g. Marine, get away from very close melee units like e.g. Zealots.
     */
    public static boolean handleAvoidCloseMeleeUnits(AUnit unit) {
        boolean isAllowedType = (unit.isGroundUnit() && unit.getType().isRangedUnit()) || unit.isWorker();
        boolean isHealthyAndHasManyHP = unit.getHitPoints() >= 60 && unit.getHPPercent() >= 100;
        
        if (isAllowedType && isHealthyAndHasManyHP) {
            
            // === Define safety distance ==============================

            double lowHealthBonus = Math.max(((100 - unit.getHPPercent()) / 25), 1.5);
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
            int enemiesNearby = Select.enemy().inRadius(safetyDistance, unit).count();
            if (enemiesNearby >= 2) {
                if (unit.isVulture()) {
                    safetyDistance += Math.max((double) enemiesNearby / 4, 3.5);
                }
                else {
                    safetyDistance += Math.max((double) enemiesNearby / 3, 2);
                }
            }
            
//            AtlantisPainter.paintTextCentered(unit.getPosition().translateByPixels(0, 12), 
//                    "" + String.format("%.1f", safetyDistance), Color.Green);
            
//            AtlantisPainter.paintCircle(unit, (int) safetyDistance * 32, Color.Green);
//            AtlantisPainter.paintTextCentered(unit, enemiesNearby + "", Color.White);

            Select<?> closeEnemies = Select.enemyRealUnits().melee().inRadius(safetyDistance, unit);
            AUnit closeEnemy = closeEnemies.nearestTo(unit);
            if (closeEnemy != null) {
                
                double base = (unit.isVulture() ? 1.5 : 2.2);
                double numberOfNearEnemiesBonus = Math.max(0.4, 
                        ((Select.enemyRealUnits().inRadius(4, unit).count() - 1) / 12));
                double archonBonus = (((Select.enemyRealUnits().ofType(AUnitType.Protoss_Archon)
                        .inRadius(5, unit)).count() > 0) ? 1.5 : 0);
                
                double dangerousDistance = base + numberOfNearEnemiesBonus + archonBonus;
                boolean isEnemyDangerouslyClose = closeEnemy.distanceTo(unit) < dangerousDistance;
                if (isEnemyDangerouslyClose) {
                    
                    boolean dontInterruptPendingAttack;
                    if (unit.isVulture()) {
                        dontInterruptPendingAttack = unit.isAttackFrame() && unit.getHPPercent() >= 30;
                    }
                    else {
                        dontInterruptPendingAttack = (unit.isAttackFrame() || unit.isStartingAttack())
                                && unit.getHPPercent() >= 30;
                    }
                    
                    if (dontInterruptPendingAttack) {
                        unit.setTooltip("Shooting" + (unit.getTarget() != null 
                                ? " " + unit.getTarget().getShortName() : ""));
                    }
                    else {
                        if (unit.runFrom(null)) {
    //                        AtlantisPainter.paintCircle(unit, (int) safetyDistance * 32, Color.Red);
    //                        AtlantisPainter.paintCircle(unit, enemyNearbyCountingRadius * 32, Color.Red);
    //                        unit.setTooltip("Melee-run " + closeEnemy.getShortName());
                            unit.setTooltip("Melee-run (" + closeEnemy.getShortName() + ")");
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
    
}
