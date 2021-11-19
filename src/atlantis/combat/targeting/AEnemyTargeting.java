package atlantis.combat.targeting;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

public class AEnemyTargeting {

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

        AUnit enemy = selectEnemyFirstByTypeThenByBestOfTypeInRange(unit, maxDistFromEnemy);
//        AUnit enemy = Select.enemy().nearestTo(unit);

        if (enemy != null) {
            APainter.paintTextCentered(unit.translateByPixels(0, 25), enemy.shortName(), Color.Green);
        }

        return enemy;
    }

    private static AUnit selectEnemyFirstByTypeThenByBestOfTypeInRange(AUnit unit, double maxDistFromEnemy) {
        AUnit enemy = selectUnitToAttackByType(unit, maxDistFromEnemy);

//        System.out.println("enemy = " + enemy + A.dist(enemy, unit));
        if (enemy == null) {
            return null;
        }

//        if (!enemy.isEnemy() || !enemy.isVisibleOnMap()) {
//            System.err.println(enemy + ", enemy:" + enemy.isEnemy() + ", alive:" + enemy.isAlive() + ", visible:" + enemy.isVisibleOnMap());
//            System.err.println("Enemy player: " + enemy.getPlayer() + ", isEnemy: " + enemy.getPlayer().isEnemy(unit.getPlayer()) + " // " + enemy.isEnemy());
//            System.err.println("Our player  : " + unit.getPlayer() + ", isEnemy: " + unit.getPlayer().isEnemy(enemy.getPlayer()) + " // " + unit.isEnemy());
//            System.err.println("AGame.getPlayerUs() : " + AGame.getPlayerUs() + ", isEnemy: " + AGame.getPlayerUs().isEnemy(enemy.getPlayer()));
//            throw new RuntimeException("This is crazy, it should never happen, but with some bugs it can happen.");
//        }

        enemy = selectWeakestEnemyOfType(enemy.type(), enemy, unit);

        return enemy;
    }

    public static boolean debug(AUnit unit) {
        return DEBUG && unit.isFirstCombatUnit();
    }

    // =========================================================

    private static AUnit selectUnitToAttackByType(AUnit unit, double maxDistFromEnemy) {
        if (maxDistFromEnemy > 30) {
            maxDistFromEnemy = 30;
        }

        // Quit early if no target at all
        if (Select.enemyRealUnits(true)
                .effVisible()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, 8)
                .count() == 0) {
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
                .excludeTypes(AUnitType.Zerg_Egg, AUnitType.Zerg_Larva)
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, 13);

        // =========================================================

        if ((target = ATargetingForSpecificUnits.target(unit)) != null) {
            if (AEnemyTargeting.debug(unit)) System.out.println("A = "+ target);
            return target;
        }

        // === Crucial units =======================================

        if ((target = ATargetingCrucial.target(unit)) != null) {
//            if (!target.type().isCarrier()) {
//                System.out.println(A.now() + "  #" + unit.getID() + " " + unit.shortName() + " > " + target.shortName());
//            }
            if (AEnemyTargeting.debug(unit)) System.out.println("B = "+ target);
            return target;
        }

        // === Important units =====================================

        if ((target = ATargetingImportant.target(unit)) != null) {
            if (AEnemyTargeting.debug(unit)) System.out.println("C = "+ target);
            return target;
        }

        // === Standard targets ====================================

        if ((target = ATargetingStandard.target(unit)) != null) {
            if (AEnemyTargeting.debug(unit)) System.out.println("D = "+ target);
            return target;
        }

        // =====

        return target;
    }

    // =========================================================

    private static AUnit selectWeakestEnemyOfType(AUnitType enemyType, AUnit enemy, AUnit ourUnit) {
//        if (ourUnit.hasWeaponRange(enemy, 0)) {
        AUnit enemyInShootRange = Select.enemies(enemyType).inShootRangeOf(ourUnit).nearestTo(ourUnit);
        if (enemyInShootRange != null) {
            return selectWeakestEnemyOfTypeInRange(enemyType, ourUnit);
        }

//        return Select.enemyRealUnits().canBeAttackedBy(ourUnit, 2).nearestTo(ourUnit);
        return selectWeakestEnemyOfTypeOutsideOfWeaponRange(enemyType, ourUnit);
    }

    private static AUnit selectWeakestEnemyOfTypeInRange(AUnitType enemyType, AUnit ourUnit) {
        Selection targets = Select.enemies(enemyType)
                .effVisible()
                .canBeAttackedBy(ourUnit, 0);

        AUnit mostWounded = targets.clone().mostWounded();
        if (mostWounded != null && mostWounded.isWounded()) {
            return mostWounded;
        }

        return targets.clone().nearestTo(ourUnit);
    }

    private static AUnit selectWeakestEnemyOfTypeOutsideOfWeaponRange(AUnitType type, AUnit ourUnit) {
        Selection targets = Select.enemies(type)
                .effVisible()
                .hasPathFrom(ourUnit);

        AUnit mostWounded = targets.clone().mostWounded();
        if (mostWounded != null && mostWounded.isWounded()) {
            return mostWounded;
        }

        return targets.clone().inShootRangeOf(1.1, ourUnit).nearestTo(ourUnit);
    }

}
