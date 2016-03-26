package atlantis.combat.micro;

import atlantis.AtlantisGame;
import atlantis.util.PositionUtil;
import atlantis.util.UnitUtil;
import atlantis.wrappers.Select;
import bwapi.Unit;
import bwapi.UnitType;

public class AtlantisEnemyTargeting {

    /**
     * For given <b>unit</b> it defines the best close range target from enemy units. The target is not
     * necessarily in the shoot range. Will return <i>null</i> if no enemy can is visible.
     */
    public static Unit defineBestEnemyToAttackFor(Unit unit) {
        boolean canAttackGround = UnitUtil.attacksGround(unit);
        boolean canAttackAir = UnitUtil.attacksAir(unit);
        Unit nearestEnemy = null;
        
        // =========================================================
        // Attack top priority units
        nearestEnemy = (Unit)Select.enemy(canAttackGround, canAttackAir)	//TODO: check safety of cast
                .inRadius(14, unit.getPosition())
                .ofType(
                        UnitType.Terran_Siege_Tank_Siege_Mode,
                        UnitType.Terran_Siege_Tank_Tank_Mode,
                        UnitType.Protoss_Reaver,
                        UnitType.Zerg_Lurker
                ).nearestTo(unit.getPosition());
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Attack nearest enemy
        if (AtlantisGame.getTimeSeconds() < 180) {
            nearestEnemy = Select.enemyRealUnits(canAttackGround, canAttackAir).nearestTo(unit.getPosition());
            if (nearestEnemy != null && nearestEnemy.getType().isWorker() 
                    && PositionUtil.distanceTo(nearestEnemy, Select.mainBase()) < 30) {
//                return null;
            }
            else {
                return nearestEnemy;
            }
        }
        
        // =========================================================
        // If no real units found, try selecting important buildings
        nearestEnemy = (Unit)Select.enemy(canAttackGround, canAttackAir)	//TODO: check safety of cast
                .ofType(UnitType.Protoss_Zealot, UnitType.Protoss_Dragoon, 
                        UnitType.Terran_Marine, UnitType.Terran_Medic, 
                        UnitType.Terran_Firebat, UnitType.Zerg_Zergling, 
                        UnitType.Zerg_Hydralisk).nearestTo(unit.getPosition());
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Try selecting defensive buildings
        nearestEnemy = (Unit)Select.enemy(canAttackGround, canAttackAir)	//TODO: check safety of cast
                .ofType(UnitType.Protoss_Photon_Cannon, UnitType.Zerg_Sunken_Colony, 
                        UnitType.Terran_Bunker).nearestTo(unit.getPosition());
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Try selecting real units
        nearestEnemy = Select.enemyRealUnits(canAttackGround, canAttackAir).nearestTo(unit.getPosition());
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // If no real units found, try selecting important buildings
        nearestEnemy = (Unit)Select.enemy(canAttackGround, canAttackAir)	//TODO: check safety of cast
                .ofType(UnitType.Protoss_Pylon, UnitType.Zerg_Spawning_Pool, 
                        UnitType.Terran_Command_Center).nearestTo(unit.getPosition());
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Okay, try targeting any-fuckin-thing
        nearestEnemy = Select.enemy(canAttackGround, canAttackAir).nearestTo(unit.getPosition());
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        return nearestEnemy;
    }
    
}
