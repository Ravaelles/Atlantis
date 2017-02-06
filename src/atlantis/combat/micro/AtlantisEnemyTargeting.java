package atlantis.combat.micro;

import atlantis.AtlantisGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.PositionUtil;

public class AtlantisEnemyTargeting {

    /**
     * For given <b>unit</b> it defines the best close range target from enemy units. The target is not
     * necessarily in the shoot range. Will return <i>null</i> if no enemy can is visible.
     */
    public static AUnit defineBestEnemyToAttackFor(AUnit unit) {
        boolean canAttackGround = unit.canAttackGroundUnits(); 
        boolean canAttackAir = unit.canAttackAirUnits(); 
        AUnit nearestEnemy = null;
        
        // =========================================================
        // Attack deadliest shit out there
        
        nearestEnemy = Select.enemy(canAttackGround, canAttackAir)
                .canBeAttackedBy(unit)
                .ofType(AUnitType.Terran_Vulture_Spider_Mine)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Attack top priority units
        
        nearestEnemy = Select.enemy(canAttackGround, canAttackAir)
                .canBeAttackedBy(unit)
                .ofType(
                        AUnitType.Terran_Siege_Tank_Siege_Mode,
                        AUnitType.Terran_Siege_Tank_Tank_Mode,
                        AUnitType.Protoss_Reaver,
                        AUnitType.Zerg_Lurker
                ).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Attack nearest enemy
        if (AtlantisGame.getTimeSeconds() < 180) {
            nearestEnemy = Select.enemyRealUnits()
                    .canBeAttackedBy(unit)
                    .nearestTo(unit);
            if (nearestEnemy != null && nearestEnemy.getType().isWorker() 
                    && PositionUtil.distanceTo(nearestEnemy, Select.mainBase()) < 30) {
//                return null;
            }
            else {
                return nearestEnemy;
            }
        }
        
        // =========================================================
        // Try selecting defensive buildings
        nearestEnemy = Select.enemy(canAttackGround, canAttackAir)	
                .ofType(AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Sunken_Colony, 
                        AUnitType.Terran_Bunker)
                .canBeAttackedBy(unit)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Try selecting real units
        nearestEnemy = Select.enemyRealUnits(canAttackGround, canAttackAir)
                .canBeAttackedBy(unit)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // If no real units found, try selecting important buildings
        nearestEnemy = Select.enemy(canAttackGround, canAttackAir)
                .ofType(AUnitType.Protoss_Pylon, AUnitType.Zerg_Spawning_Pool, 
                        AUnitType.Terran_Command_Center)
                .canBeAttackedBy(unit)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Okay, try targeting any-fuckin-thing
        nearestEnemy = Select.enemy(canAttackGround, canAttackAir)
                .canBeAttackedBy(unit)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        return nearestEnemy;
    }
    
}
