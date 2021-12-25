package atlantis.combat.targeting;

import atlantis.debug.APainter;
import atlantis.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

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
//        AUnit enemy = Select.enemy().nearestTo(unit);

        if (enemy != null) {
            APainter.paintTextCentered(unit.translateByPixels(0, 25), enemy.name(), Color.Green);
        }

        return enemy;
    }

    public static AUnit defineBestEnemyToAttackFor(AUnit unit) {
        return defineBestEnemyToAttackFor(unit, 15);
    }

    public static boolean debug(AUnit unit) {
//        return DEBUG && unit.isFirstCombatUnit();
        return DEBUG;
    }

    // =========================================================

    private static AUnit defineTarget(AUnit unit, double maxDistFromEnemy) {
        AUnit enemy = selectUnitToAttackByType(unit, maxDistFromEnemy);
        if (enemy == null) {
            return null;
        }

        return selectWeakestEnemyOfType(enemy.type(), unit);
    }

    // =========================================================

    private static AUnit selectWeakestEnemyOfType(AUnitType enemyType, AUnit ourUnit) {

        // Most wounded enemy IN RANGE
        AUnit enemy = selectWeakestEnemyOfTypeWithWeaponRange(enemyType, ourUnit, 0);
        if (enemy != null) {
            return enemy;
        }

        // Most wounded enemy ALMOST IN RANGE
        enemy = selectWeakestEnemyOfTypeWithWeaponRange(enemyType, ourUnit, 1.2);
        if (enemy != null) {
            return enemy;
        }

        // Distant enemies
        enemy = selectWeakestEnemyOfTypeWithWeaponRange(enemyType, ourUnit, 40);
        if (enemy != null) {
            return enemy;
        }

//        // Most wounded enemy ALMOST IN RANGE
//        enemy = selectWeakestEnemyOfTypeWithWeaponRange(enemyType, ourUnit, 2.2);
//        if (enemy != null) {
//            return enemy;
//        }

        // =====================================================================
        // Couldn't find enemy of given type in/near weapon range. Change target

        // Most wounded enemy OF DIFFERENT TYPE, but IN RANGE
        enemy = Select.enemyRealUnits().canBeAttackedBy(ourUnit, 0.1).mostWounded();
        if (enemy != null) {
            return enemy;
        }

//        System.err.println("Man, how comes we're here? " + ourUnit + " // " + ourUnit.enemiesNearby().count());

        return Select.enemyRealUnits().canBeAttackedBy(ourUnit, 14).nearestTo(ourUnit);
//        enemy = selectWeakestEnemyOfTypeOutsideOfWeaponRange(enemyType, ourUnit, 1.2);
//        if (enemy != null) {
//            return enemy;
//        }
//
//        return selectWeakestEnemyOfTypeOutsideOfWeaponRange(enemyType, ourUnit, 16);
    }

    private static AUnit selectWeakestEnemyOfTypeInRange(AUnitType enemyType, AUnit ourUnit) {
        Selection targets = Select.enemies(enemyType)
                .effVisible()
                .inShootRangeOf(ourUnit);

        AUnit mostWounded = targets.clone().mostWounded();
        if (mostWounded != null && mostWounded.isWounded()) {
            return mostWounded;
        }

        return targets.clone().nearestTo(ourUnit);
    }

    private static AUnit selectWeakestEnemyOfTypeWithWeaponRange(AUnitType type, AUnit ourUnit, double extraRange) {
        Selection targets = Select.enemies(type)
                .canBeAttackedBy(ourUnit, extraRange)
                .effVisible();
//                .hasPathFrom(ourUnit);

        AUnit mostWounded = targets.clone().inShootRangeOf(extraRange, ourUnit).mostWounded();
        if (mostWounded != null && mostWounded.isWounded()) {
            return mostWounded;
        }

        return targets.clone().nearestTo(ourUnit);
    }

    // =========================================================

    private static AUnit selectUnitToAttackByType(AUnit unit, double maxDistFromEnemy) {
        if (maxDistFromEnemy > 30) {
            maxDistFromEnemy = 30;
        }

//        Select.enemyRealUnits(true)
//                .effVisible()
//                .inRadius(maxDistFromEnemy, unit)
//                .canBeAttackedBy(unit, 8)
//                .print();

        Selection targets = Select.enemyRealUnits(true)
                .effVisible()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, 15);

        // Quit early if no target at all
        if (
                Select.enemyRealUnits(true)
                .effVisible()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, 15)
                .count() == 0
        ) {
            if (debug(unit)) {
                System.out.println("PreA quit");
//                System.out.println(Select.enemyRealUnits(true).count());
//                System.out.println(Select.enemyRealUnits(true)
//                        .effVisible().count());
//                System.out.println(Select.enemyRealUnits(true)
//                        .effVisible()
//                        .inRadius(maxDistFromEnemy, unit).count());
//                System.out.println(Select.enemyRealUnits(true)
//                        .effVisible()
//                        .inRadius(maxDistFromEnemy, unit)
//                        .canBeAttackedBy(unit, 8).count());
            }
            return null;
        }

        // =========================================================

        AUnit target;
        enemyBuildings = Select.enemy()
                .buildings()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, 13);
        enemyUnits = Select.enemyRealUnits(false)
                .effVisible()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, 13);

//        enemyBuildings.print();
//        enemyUnits.print();

        // =========================================================

        if ((target = ATargetingForSpecificUnits.target(unit)) != null) {
            if (ATargeting.debug(unit)) System.out.println("A = "+ target);
            return target;
        }

        // === Crucial units =======================================

        if ((target = ATargetingCrucial.target(unit)) != null) {
//            if (!target.type().isCarrier()) {
//                System.out.println(A.now() + "  #" + unit.id() + " " + unit.name() + " > " + target.name());
//            }
            if (ATargeting.debug(unit)) System.out.println("B = "+ target);
            return target;
        }

        // === Important units =====================================

        if ((target = ATargetingImportant.target(unit)) != null) {
            if (ATargeting.debug(unit)) System.out.println("C = "+ target);
            return target;
        }

        // === Standard targets ====================================

        if ((target = ATargetingStandard.target(unit)) != null) {
            if (ATargeting.debug(unit)) System.out.println("D = "+ target);
            return target;
        }

        // =====

        return target;
    }

}
