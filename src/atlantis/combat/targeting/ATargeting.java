package atlantis.combat.targeting;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

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

    private static AUnit selectWeakestEnemyOfType(AUnitType enemyType, AUnit unit) {
        double MOST_WOUNDED_EXTRA = 0;

        // Most wounded enemy IN RANGE
        AUnit enemy = selectWeakestEnemyOfTypeWithWeaponRange(enemyType, unit, unit.isRanged() ? MOST_WOUNDED_EXTRA : 0);
        if (enemy != null) {
//            unit.addLog("AttackClose");
            return enemy;
        }

        // Most wounded enemy some distance from away
        enemy = selectWeakestEnemyOfTypeWithWeaponRange(enemyType, unit, 5);
        if (enemy != null) {
//            unit.addLog("AttackDistant");
            return enemy;
        }

        // =====================================================================
        // Couldn't find enemy of given type in/near weapon range. Change target

        // Most wounded enemy OF DIFFERENT TYPE, but IN RANGE
        enemy = Select.enemyRealUnits().canBeAttackedBy(unit, MOST_WOUNDED_EXTRA).mostWounded();
        if (enemy != null) {
//            unit.addLog("AttackMostWounded");
            return enemy;
        }

//        int nearbyEnemiesCount = unit.enemiesNear().inRadius(4, unit).count();
//        System.err.println("Man, how comes we're here? " + unit + " // " + nearbyEnemiesCount);
//        if (nearbyEnemiesCount > 0) {
//            A.printStackTrace("Lets debug this");
//        }

        double maxDistToEnemy = unit.mission() != null && unit.isMissionDefend() ? 6 : 999;

        return Select.enemyRealUnits().canBeAttackedBy(unit, maxDistToEnemy).nearestTo(unit);
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

    private static AUnit selectWeakestEnemyOfTypeWithWeaponRange(AUnitType type, AUnit ourUnit, double extraRange) {
//        Selection targets = ourUnit.enemiesNear()
        Selection targets = Select.enemies(type)
                .ofType(type)
                .canBeAttackedBy(ourUnit, extraRange)
                .effVisible();
//                .hasPathFrom(ourUnit);

        AUnit mostWounded = targets.clone().inShootRangeOf(extraRange, ourUnit).mostWounded();
        if (mostWounded != null && mostWounded.isWounded()) {
            return mostWounded;
        }

        HasPosition relativeTo = ourUnit.squadCenter() != null ? ourUnit.squadCenter() : ourUnit;
        return targets.clone().nearestTo(relativeTo);
    }

    // =========================================================

    private static AUnit selectUnitToAttackByType(AUnit unit, double maxDistFromEnemy) {

        // Quit early if no target at all
        if (
            unit.enemiesNear()
                .effVisible()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, 15)
                .isEmpty()
        ) {
            return null;
        }

        // =========================================================

        AUnit target;
//        enemyBuildings = unit.enemiesNear()
        enemyBuildings = Select.enemyRealUnits()
                .buildings()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, 14);
        enemyUnits = Select.enemyRealUnits()
                .nonBuildings()
                .effVisible()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, 14);

//        enemyBuildings.print();
//        enemyUnits.print();

        // =========================================================

        if ((target = ATargetingForSpecificUnits.target(unit)) != null) {
            if (ATargeting.debug(unit)) System.out.println("A = "+ target);
            return target;
        }

        // === Crucial units =======================================

        if ((target = ATargetingCrucial.target(unit)) != null) {
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
