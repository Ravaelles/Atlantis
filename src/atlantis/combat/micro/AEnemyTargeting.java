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
    public static AUnit defineBestEnemyToAttackFor(AUnit unit, double maxDistFromEnemy) {
        return selectUnitToAttackByType(unit, maxDistFromEnemy);
    }
    
    // =========================================================

    private static AUnit selectUnitToAttackByType(AUnit unit, double maxDistFromEnemy) {
        if (maxDistFromEnemy > 1000) {
            maxDistFromEnemy = 30;
        }

        if (Select.enemyRealUnits()
                .canBeAttackedBy(unit, false)
                .inRadius(maxDistFromEnemy, unit)
                .count() == 0) {
            return null;
        }

        Select<AUnit> allEnemyUnitsThatCanBeAttacked = Select.enemy()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, false);
        Select<AUnit> enemyRealUnitsThatCanBeAttacked = Select.enemyRealUnits()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, false);

        AUnit nearestEnemy = null;

        // =========================================================
        // Attack deadliest shit out there
        
        nearestEnemy = enemyRealUnitsThatCanBeAttacked.clone()
                .ofType(
                        AUnitType.Zerg_Scourge,
                        AUnitType.Zerg_Defiler,
                        AUnitType.Terran_Vulture_Spider_Mine,
                        AUnitType.Protoss_Dark_Templar
                )
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }

        // =========================================================
        // Attack top priority units

        nearestEnemy = enemyRealUnitsThatCanBeAttacked.clone()
                .ofType(
                        AUnitType.Terran_Siege_Tank_Siege_Mode,
                        AUnitType.Terran_Siege_Tank_Tank_Mode,
                        AUnitType.Protoss_Carrier,
                        AUnitType.Protoss_Reaver,
                        AUnitType.Zerg_Lurker
                ).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Attack nearest enemy

//        if (AGame.getTimeSeconds() < 180) {
//            nearestEnemy = allEnemyUnitsThatCanBeAttacked.clone()
//                    .nearestTo(unit);
//            if (nearestEnemy != null && nearestEnemy.getType().isWorker()
//                    && PositionUtil.distanceTo(nearestEnemy, Select.mainBase()) < 30) {
////                return null;
//            }
//            else {
//                return nearestEnemy;
//            }
//        }

        // =========================================================
        // Target real units

        nearestEnemy = enemyRealUnitsThatCanBeAttacked.clone()
                .combatUnits()
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }

        // =========================================================
        // Defensive buildings

        nearestEnemy = allEnemyUnitsThatCanBeAttacked.clone()
                .ofType(AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Sunken_Colony,
                        AUnitType.Terran_Bunker)
                .canBeAttackedBy(unit, false)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }

        // =========================================================
        // Target real units

        nearestEnemy = enemyRealUnitsThatCanBeAttacked.clone()
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }

        // =========================================================
        // If no real units found, try selecting important buildings
        nearestEnemy = allEnemyUnitsThatCanBeAttacked.clone()
                .ofType(AUnitType.Protoss_Pylon, AUnitType.Zerg_Spawning_Pool,
                        AUnitType.Terran_Command_Center)
                .canBeAttackedBy(unit, false)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }

        // =========================================================
        // Okay, try targeting any-fuckin-thing

        nearestEnemy = allEnemyUnitsThatCanBeAttacked.clone()
                .canBeAttackedBy(unit, false)
                .nearestTo(unit);

        return nearestEnemy;
    }
    
}
