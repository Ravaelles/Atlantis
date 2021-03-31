package atlantis.combat.micro;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.PositionUtil;

public class AEnemyTargeting {

    /**
     * For given <b>unit</b> it defines the best close range target from enemy units. The target is not
     * necessarily in the shoot range. Will return <i>null</i> if no enemy can is visible.
     */
    public static AUnit defineBestEnemyToAttackFor(AUnit unit) {
        return selectUnitToAttackByType(unit);
//        AUnit preSelectedEnemy = selectUnitToAttackByType(unit);
//        AUnit finalEnemyToAttack = null;
//
//        // =========================================================
//        // If found enemy, try to attack enemy of the same type, with fewest HP
//
//        if (preSelectedEnemy != null && (unit.isVulture() && preSelectedEnemy.distanceTo(unit) > 3)) {
//            AUnitType enemyType = preSelectedEnemy.getType();
//            int weaponRange = unit.getWeaponRangeAgainst(preSelectedEnemy);
//
//            // Find most wounded enemy unit of the same type within shoot range
//            finalEnemyToAttack = Select.enemyOfType(enemyType).inRadius(weaponRange, unit).lowestHealth();
//        }
//        else {
//            finalEnemyToAttack = preSelectedEnemy;
//        }
//
//        return finalEnemyToAttack;
    }
    
    // =========================================================

    private static AUnit selectUnitToAttackByType(AUnit unit) {
        if (Select.enemyRealUnits()
                .canBeAttackedBy(unit, false)
                .inRadius(14, unit)
                .count() == 0) {
            return null;
        }
        
        AUnit nearestEnemy = null;
        
        Select<AUnit> enemiesThatCanBeAttackedSelector = Select.enemyRealUnits()
                .canBeAttackedBy(unit, false);

        // =========================================================
        // Attack deadliest shit out there
        
        nearestEnemy = Select.enemyRealUnits().visible()
                .canBeAttackedBy(unit, false)
                .ofType(AUnitType.Terran_Vulture_Spider_Mine)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Attack top priority units
        
        nearestEnemy = enemiesThatCanBeAttackedSelector
                .canBeAttackedBy(unit, false)
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
        if (AGame.getTimeSeconds() < 180) {
            nearestEnemy = Select.enemyRealUnits()
                    .canBeAttackedBy(unit, false)
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
        nearestEnemy = enemiesThatCanBeAttackedSelector	
                .ofType(AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Sunken_Colony, 
                        AUnitType.Terran_Bunker)
                .canBeAttackedBy(unit, false)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Try selecting real units
        nearestEnemy = Select.enemyRealUnits()
                .canBeAttackedBy(unit, false)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // If no real units found, try selecting important buildings
        nearestEnemy = enemiesThatCanBeAttackedSelector
                .ofType(AUnitType.Protoss_Pylon, AUnitType.Zerg_Spawning_Pool, 
                        AUnitType.Terran_Command_Center)
                .canBeAttackedBy(unit, false)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Okay, try targeting any-fuckin-thing
        nearestEnemy = enemiesThatCanBeAttackedSelector
                .canBeAttackedBy(unit, false)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        return nearestEnemy;
    }
    
}
