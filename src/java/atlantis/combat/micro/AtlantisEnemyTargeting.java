package atlantis.combat.micro;

import atlantis.AtlantisGame;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class AtlantisEnemyTargeting {

    /**
     * For given <b>unit</b> it defines the best close range target from enemy units. The target is not
     * necessarily in the shoot range. Will return <i>null</i> if no enemy can is visible.
     */
    public static Unit defineBestEnemyToAttackFor(Unit unit) {
        boolean canAttackGround = unit.canAttackGroundUnits();
        boolean canAttackAir = unit.canAttackAirUnits();
        Unit nearestEnemy = null;
        
        // =========================================================
        // Attack top priority units
        nearestEnemy = SelectUnits.enemy(canAttackGround, canAttackAir)
                .inRadius(14, unit)
                .ofType(
                        UnitType.UnitTypes.Terran_Siege_Tank_Siege_Mode,
                        UnitType.UnitTypes.Terran_Siege_Tank_Tank_Mode,
                        UnitType.UnitTypes.Protoss_Reaver,
                        UnitType.UnitTypes.Zerg_Lurker
                ).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Attack nearest enemy
        if (AtlantisGame.getTimeSeconds() < 180) {
            nearestEnemy = SelectUnits.enemyRealUnits(canAttackGround, canAttackAir).nearestTo(unit);
            if (nearestEnemy != null && nearestEnemy.isWorker() 
                    && nearestEnemy.distanceTo(SelectUnits.mainBase()) < 30) {
//                return null;
            }
            else {
                return nearestEnemy;
            }
        }
        
        // =========================================================
        // If no real units found, try selecting important buildings
        nearestEnemy = SelectUnits.enemy(canAttackGround, canAttackAir)
                .ofType(UnitType.UnitTypes.Protoss_Zealot, UnitType.UnitTypes.Protoss_Dragoon, 
                        UnitType.UnitTypes.Terran_Marine, UnitType.UnitTypes.Terran_Medic, 
                        UnitType.UnitTypes.Terran_Firebat, UnitType.UnitTypes.Zerg_Zergling, 
                        UnitType.UnitTypes.Zerg_Hydralisk).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Try selecting defensive buildings
        nearestEnemy = SelectUnits.enemy(canAttackGround, canAttackAir)
                .ofType(UnitType.UnitTypes.Protoss_Photon_Cannon, UnitType.UnitTypes.Zerg_Sunken_Colony, 
                        UnitType.UnitTypes.Terran_Bunker).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Try selecting real units
        nearestEnemy = SelectUnits.enemyRealUnits(canAttackGround, canAttackAir).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // If no real units found, try selecting important buildings
        nearestEnemy = SelectUnits.enemy(canAttackGround, canAttackAir)
                .ofType(UnitType.UnitTypes.Protoss_Pylon, UnitType.UnitTypes.Zerg_Spawning_Pool, 
                        UnitType.UnitTypes.Terran_Command_Center).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Okay, try targeting any-fuckin-thing
        nearestEnemy = SelectUnits.enemy(canAttackGround, canAttackAir).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        return nearestEnemy;
    }
    
}
