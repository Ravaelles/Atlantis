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
        AUnit enemy = selectUnitToAttackByType(unit, maxDistFromEnemy);
        if (enemy == null) {
            return null;
        }

        return selectWeakestEnemyInRangeOfType(enemy.getType(), unit);
    }

    // =========================================================

    private static AUnit selectUnitToAttackByType(AUnit unit, double maxDistFromEnemy) {
        if (maxDistFromEnemy > 1000) {
            maxDistFromEnemy = 30;
        }

        if (Select.enemyRealUnits(true)
                .canBeAttackedBy(unit, false)
                .inRadius(maxDistFromEnemy, unit)
                .count() == 0) {
            return null;
        }

        Select<AUnit> enemyBuildings = Select.enemy()
                .buildings()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, false);
        Select<AUnit> enemyRealUnitsThatCanBeAttacked = Select.enemyRealUnits(false)
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, true);

        AUnit nearestEnemy = null;

        // =========================================================
        // Attack mines
        
        nearestEnemy = enemyRealUnitsThatCanBeAttacked.clone()
                .ofType(AUnitType.Terran_Vulture_Spider_Mine)
                .inRadius(12, unit)
                .canBeAttackedBy(unit, true)
                .randomWithSeed(unit.getID());
        if (nearestEnemy != null) {
            return nearestEnemy;
        }

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
                        AUnitType.Protoss_Archon,
                        AUnitType.Protoss_Observer,
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
                .canBeAttackedBy(unit, true)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }

        // =========================================================
        // Defensive buildings

        nearestEnemy = enemyBuildings.clone()
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
        // Bases

        nearestEnemy = enemyBuildings.clone()
                .bases()
                .canBeAttackedBy(unit, false)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }

        // =========================================================
        // Important buildings

        nearestEnemy = enemyBuildings.clone()
                .ofType(
                        AUnitType.Zerg_Spawning_Pool,
                        AUnitType.Zerg_Spire,
                        AUnitType.Terran_Armory,
                        AUnitType.Protoss_Fleet_Beacon,
                        AUnitType.Protoss_Templar_Archives
                )
                .canBeAttackedBy(unit, false)
                .nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }

        // =========================================================
        // If no real units found, try selecting important buildings

//        nearestEnemy = enemyBuildings.clone()
//                .ofType(AUnitType.Protoss_Pylon, AUnitType.Zerg_Spawning_Pool,
//                        AUnitType.Terran_Command_Center)
//                .canBeAttackedBy(unit, false)
//                .nearestTo(unit);
//        if (nearestEnemy != null) {
//            return nearestEnemy;
//        }

        // =========================================================
        // Okay, try targeting any-fuckin-thing

        nearestEnemy = Select.enemyRealUnits(true)
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, false)
                .nearestTo(unit);

        return nearestEnemy;
    }

    private static AUnit selectWeakestEnemyInRangeOfType(AUnitType enemyType, AUnit ourUnit) {
        Select<AUnit> targets = Select.enemyOfType(enemyType).visible().canBeAttackedBy(ourUnit, true);

        AUnit mostWounded = targets.clone().mostWounded();
        if (mostWounded != null && mostWounded.isWounded()) {
            return mostWounded;
        }

        AUnit nearest = targets.nearestTo(ourUnit);
        if (nearest != null) {
            return nearest;
        }

        return Select.enemyOfType(enemyType).visible().canBeAttackedBy(ourUnit, false).nearestTo(ourUnit);
    }
    
}
