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
    
    private static AUnit nearestEnemy = null;
//    private static Select<AUnit> enemyRealUnitsSelector = null;
    
    // =========================================================

    /**
     * If unit is ranged unit like e.g. Marine, get away from very close melee units like e.g. Zealots.
     */
    public static boolean avoidCloseMeleeUnits(AUnit unit) {
        System.out.println(">>>>>> " + AGame.getTimeFrames());
        if (shouldSkip(unit)) {
            return true;
        }

        // === Define and remember selector for enemy combat units ==
        
//        enemyRealUnitsSelector = Select.enemyRealUnits().combatUnits();
        if (Select.enemyRealUnits().combatUnits().inRadius(7, unit).count() <= 0) {
            return false;
        }

        // === Define safety distance ===============================
        
        boolean isEnemyDangerouslyClose = isEnemyDangerouslyClose(unit);
        if (!isEnemyDangerouslyClose) {
            System.out.println("                   enemy Far");
            return false;
        }
        
        boolean shouldInterruptPendingAttack = shouldInterruptPendingAttack(unit);

        // =========================================================
        // Don't run, because unit is JUST SHOOTING
        
        if (!shouldInterruptPendingAttack) {
            unit.setTooltip("Shoot " + (unit.getTarget() != null
                    ? " " + unit.getTarget().getShortName() : ""));
            return true;
        } 

        // === Run the fuck outta here =============================
        
        else {
            APainter.paintTextCentered(unit.getPosition().translateByPixels(0, -12), "RUN", Color.Red);
            if (unit.runFrom(nearestEnemy)) {
                unit.setTooltip("Melee-run");
                return true;
            } else {
                unit.setTooltip("ERROR_RUN");
                System.err.println("ERROR_RUN for " + unit.getShortNamePlusId());
//                AGame.sendMessage("ERROR_RUN for " + unit.getShortNamePlusId());
                return false;
            }
        }
    }

    // =========================================================
    
    private static boolean shouldSkip(AUnit unit) {

        // === Issue orders every 3 frames or so ========================================
//        if (unit.getFramesSinceLastOrderWasIssued() <= 2 && !unit.isIdle()) {

            // Scout mustn't exit here, otherwise scouting behavior will override this behavior.
            if (unit.isScout()) {
                return true;
            }
//        }

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

    private static boolean isEnemyDangerouslyClose(AUnit unit) {
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
        int enemiesNearby = Select.enemyRealUnits().combatUnits().inRadius(safetyDistance, unit).count();
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
        Select<?> closeEnemies = Select.enemyRealUnits().combatUnits().melee().inRadius(safetyDistance, unit);
        nearestEnemy = closeEnemies.nearestTo(unit);
        unit.setCachedNearestMeleeEnemy(nearestEnemy);

//            APainter.paintCircleFilled(unit.getPosition(), 11, Color.White);
        if (nearestEnemy != null) {
            return isEnemyCriticallyClose(unit, nearestEnemy);
        }

        return false;
    }

    private static boolean shouldInterruptPendingAttack(AUnit unit) {
        if (!unit.isAttackFrame() && !unit.isStartingAttack()) {
            return false;
        }
        
        AUnit nearestEnemy = unit.getCachedNearestMeleeEnemy();
        double enemyDistance = nearestEnemy.distanceTo(unit);

        if (unit.isVulture()) {
            return enemyDistance < 3.5 && unit.getHPPercent() < 40;
        } else {
//            return enemyDistance < 1.8
//                    && (unit.isAttackFrame() || unit.isStartingAttack()) && unit.getHPPercent() >= 30;
            return (enemyDistance < 1.2 && Select.enemyRealUnits().combatUnits().inRadius(2.5, unit).count() <= 1)
                    || (enemyDistance < 1.8 && Select.enemyRealUnits().combatUnits().inRadius(3, unit).count() >= 2)
                    || (enemyDistance < 2.5 && unit.getHPPercent() <= 30);
        }
    }

    private static boolean isEnemyCriticallyClose(AUnit unit, AUnit nearestEnemy) {
        double baseCriticalDistance = (unit.isVulture() ? 1.9 : 2.2);
        double healthBonus = unit.getHPPercent() < 30 ? 0.5 : 0;
        double numberOfNearEnemiesBonus = Math.max(0.4,
                ((Select.enemyRealUnits().inRadius(4, unit).count() - 1) / 12));
        double archonBonus = (((Select.enemyRealUnits().combatUnits().ofType(AUnitType.Protoss_Archon)
                .inRadius(5, unit)).count() > 0) ? 1 : 0);

        double criticalDistance = baseCriticalDistance + numberOfNearEnemiesBonus
                + healthBonus + archonBonus;
        double enemyDistance = nearestEnemy.distanceTo(unit);
        boolean isEnemyCriticallyClose = enemyDistance < criticalDistance;

        if (isEnemyCriticallyClose) {
            APainter.paintCircle(unit.getPosition(), (int) (32 * criticalDistance), Color.Red);
            APainter.paintLine(unit, nearestEnemy, Color.Red);
            return true;
        }
        else {
            APainter.paintCircle(unit.getPosition(), (int) (32 * criticalDistance), Color.Green);
            return false;
        }
    }

}
