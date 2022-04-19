package atlantis.combat.targeting;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

import java.util.List;

import static atlantis.combat.micro.AAttackEnemyUnit.reasonNotToAttack;

public class ATargeting {

//    protected static final boolean DEBUG = true;
    protected static final boolean DEBUG = false;

    protected static Selection enemyBuildings;
    protected static Selection enemyUnits;

    /**
     * For given <b>unit</b> it defines the best close range target from enemy units. The target is not
     * necessarily in the shoot range. Will return <i>null</i> if no enemy can is visible.
     */
    public static AUnit defineBestEnemyToAttackFor(AUnit unit, double maxDistFromEnemy) {
//        if (true) return null;

        AUnit enemy = defineTarget(unit, maxDistFromEnemy);
//        AUnit enemy = Select.enemy().canBeAttackedBy(unit, 30).nearestTo(unit);

//        if (enemy != null) {
//            APainter.paintTextCentered(unit.translateByPixels(0, 25), enemy.name(), Color.Green);
//        }

        if (enemy == null) {
            reasonNotToAttack = null;
        }

        return enemy;
    }

    public static AUnit defineBestEnemyToAttackFor(AUnit unit) {
        return defineBestEnemyToAttackFor(unit, AAttackEnemyUnit.MAX_DIST_TO_ATTACK);
    }

    // =========================================================

    private static AUnit defineTarget(AUnit unit, double maxDistFromEnemy) {
        AUnit enemy = selectUnitToAttackByType(unit, maxDistFromEnemy);
//        System.out.println("defineTarget = " + enemy);

//        if (enemy == null) {
//            return null;
//        }

        // Somewhat ugly fix for when targeting goes wrong
//        if (enemy == null) {
//            enemy = unit.enemiesNear().canBeAttackedBy(unit, 1.2).nearestTo(unit);
//        }

        if (enemy == null) {
//            enemy = unit.enemiesNear().havingPosition().effVisible().groundUnits().nearestTo(unit);
            enemy = unit.enemiesNear().realUnitsButAllowBuildings().canBeAttackedBy(unit, 0).nearestTo(unit);
        }

//        if (enemy == null) {
//            Selection possible = unit.enemiesNear().visibleOnMap().havingAtLeastHp(1).effVisible().groundUnits();
//            if (possible.atLeast(1) && unit.canAttackGroundUnits()) {
//                System.err.println(unit + " return NULL target WTF");
//                possible.print("These could be targetted");
//                System.err.println("As a fix return: " + possible.nearestTo(unit));
//                return possible.nearestTo(unit);
//            }
//            return null;
//        }

        if (enemy == null) {
            return null;
        }

//        System.out.println("enemy.type() = " + enemy.type());
        AUnit weakestEnemy = selectWeakestEnemyOfType(enemy.type(), unit);
//        System.out.println("weakestEnemy = " + weakestEnemy);

        if (weakestEnemy != null && unit.isTank()) {
            AUnit tank = handleTanksSpecially(unit, weakestEnemy);
            if (tank != null) {
                return tank;
            }
        }

        return weakestEnemy;
    }

    // =========================================================

    private static AUnit selectWeakestEnemyOfType(AUnitType enemyType, AUnit unit) {

        // Most wounded enemy IN RANGE
        AUnit enemy = selectWeakestEnemyOfType(enemyType, unit, 0);
//        System.out.println("enemy A = " + enemy);
        if (enemy != null) {
//            unit.addLog("AttackClose");
            return enemy;
        }

        // Most wounded enemy some distance from away
        enemy = selectWeakestEnemyOfType(enemyType, unit, 6);
//        System.out.println("enemy B = " + enemy);
        if (enemy != null) {
//            unit.addLog("AttackDistant");
            return enemy;
        }

        // =====================================================================
        // Couldn't find enemy of given type in/near weapon range. Change target

        // Nearest enemy
        enemy = Select.enemyRealUnits().canBeAttackedBy(unit, 0).nearestTo(unit);
        if (enemy != null) {
            unit.addLog("AttackNearest");
            return enemy;
        }

//        // Most wounded enemy OF DIFFERENT TYPE, but IN RANGE
//        enemy = Select.enemyRealUnits().canBeAttackedBy(unit, 0).mostWounded();
//        if (enemy != null) {
////            unit.addLog("AttackMostWounded");
//            return enemy;
//        }
//
////        int nearbyEnemiesCount = unit.enemiesNear().inRadius(4, unit).count();
////        System.err.println("Man, how comes we're here? " + unit + " // " + nearbyEnemiesCount);
////        if (nearbyEnemiesCount > 0) {
////            A.printStackTrace("Lets debug this");
////        }
//
//        double maxDistToEnemy = unit.mission() != null && unit.isMissionDefend() ? 6 : 999;
//
//        return Select.enemyRealUnits().canBeAttackedBy(unit, maxDistToEnemy).nearestTo(unit);

        return null;
    }

//    private static AUnit selectWeakestEnemyOfTypeInRange(AUnitType type, AUnit ourUnit) {
//        Selection targets = ourUnit.enemiesNear()
//                .ofType(type)
//                .effVisible()
//                .inShootRangeOf(ourUnit);
//
//        AUnit mostWounded = targets.clone().mostWounded();
//        if (mostWounded != null && mostWounded.isWounded()) {
//            return mostWounded;
//        }
//
//        return targets.clone().nearestTo(ourUnit);
//    }

    private static AUnit selectWeakestEnemyOfType(AUnitType type, AUnit ourUnit, double extraRange) {
//        Selection targets = ourUnit.enemiesNear()
        Selection targets = Select.enemies(type)
                .ofType(type)
                .canBeAttackedBy(ourUnit, extraRange)
                .effVisible();
//                .hasPathFrom(ourUnit);

        // It makes sense to focus fire on units that have lot of HP
        boolean shouldFocusFire = ourUnit.friendsNearCount() <= 7
            || (type.maxHp() > 35 && !type.isWorker());
        if (shouldFocusFire) {
//            .inShootRangeOf(extraRange, ourUnit)
            AUnit mostWounded = targets.mostWounded();
            if (mostWounded != null && mostWounded.isWounded() && mostWounded.hp() >= 21) {
                return mostWounded;
            }
        }

        // For units with low HP (Zerglings, workers), it makes sense to spread the fire across multiple units,
        // otherwise enemy that dies consumes unit's cooldown and effectively - it stops shooting at all.
        else if (targets.notEmpty() && ourUnit.isRanged()) {
            List<AUnit> enemies = targets.sortByHealth().limit(Enemy.zerg() ? 4 : 2).list();

            // Randomize enemy target based on unit id
            AUnit randomPeasant = enemies.get(ourUnit.id() % enemies.size());
            if (randomPeasant != null) {
                return randomPeasant;
            }
        }

        HasPosition relativeTo = ourUnit.squadCenter() != null ? ourUnit.squadCenter() : ourUnit;
        return targets.clone().nearestTo(relativeTo);
    }

    private static AUnit handleTanksSpecially(AUnit unit, AUnit weakestEnemy) {
        if (weakestEnemy.enemiesNear().inRadius(2, unit).notEmpty()) {
            AUnit tankTarget = unit.enemiesNear().combatUnits().canBeAttackedBy(unit, 0).mostDistantTo(unit);
            if (tankTarget != null) {
                return tankTarget;
            }
        }

        return null;
    }

    // =========================================================

    private static AUnit selectUnitToAttackByType(AUnit unit, double maxDistFromEnemy) {

        // Quit early if no target at all
        if (
            unit.enemiesNear()
                .effVisible()
                .inRadius(maxDistFromEnemy, unit)
                .isEmpty()
        ) {
//            System.out.println("No enemies near for " + unit + " in dist=" + maxDistFromEnemy);
            return null;
        }

        // =========================================================

        AUnit target;
        enemyBuildings = Select.enemyRealUnits(true, false, true)
                .buildings()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, maxDistFromEnemy);
        enemyUnits = Select.enemyRealUnits()
                .nonBuildings()
                .inRadius(maxDistFromEnemy, unit)
                .maxGroundDist(maxDistFromEnemy, unit)
                .effVisible();

//        Select.enemyRealUnits().print();
//        enemyBuildings.print();
//        enemyUnits.print();

//        if (unit.isDragoon()) {
//            System.out.println("--- Enemy units near " + unit.idWithHash() + " (" + enemyUnits.size() + ") ---");
//            for (AUnit enemy : enemyUnits.list()) {
//                System.out.println(enemy.getClass() + " " + enemy.toString() + " / " + A.dist(unit, enemy));
//            }
//        }

//        System.out.println("@@@@@@@@@@ size = " + enemyUnits.size());
//        System.out.println("@@@@@@@@@@a " + Select.enemyRealUnits().size());
//        System.out.println("@@@@@@@@@@b " + Select.enemyRealUnits().nonBuildings().size());
//        System.out.println("@@@@@@@@@@c " + Select.enemyRealUnits().nonBuildings().effVisible().size());
//        System.out.println("@@@@@@@@@@d " + Select.enemyRealUnits().nonBuildings().effVisible().inRadius(maxDistFromEnemy, unit).size());
//        System.out.println("@@@@@@@@@@e " + Select.enemyRealUnits().nonBuildings().effVisible().inRadius(500, unit).size());

        // =========================================================

//        if ((target = ATargetingForSpecificUnits.target(unit)) != null) {
//            if (ATargeting.DEBUG) System.out.println("A = "+ target);
//            return target;
//        }

        // === Crucial units =======================================

        if ((target = ATargetingCrucial.target(unit)) != null) {
//            if (ATargeting.DEBUG) System.out.println("B = "+ target);
            return target;
        }

        // === Important units =====================================

        if ((target = ATargetingImportant.target(unit)) != null) {
//            if (ATargeting.DEBUG) System.out.println("C = "+ target);
            return target;
        }

        // === Standard targets ====================================

        if ((target = ATargetingStandard.target(unit)) != null) {
//            if (ATargeting.DEBUG) System.out.println("D = "+ target);
            return target;
        }

        // =====

        return target;
    }

}
