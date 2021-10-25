package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.A;

public class AEnemyTargeting {

//    protected static final boolean DEBUG = true;
    protected static final boolean DEBUG = false;

    protected static Select<? extends AUnit> enemyBuildings;
    protected static Select<? extends AUnit> enemyUnits;

    /**
     * For given <b>unit</b> it defines the best close range target from enemy units. The target is not
     * necessarily in the shoot range. Will return <i>null</i> if no enemy can is visible.
     */
    public static AUnit defineBestEnemyToAttackFor(AUnit unit, double maxDistFromEnemy) {
        AUnit enemy = selectUnitToAttackByType(unit, maxDistFromEnemy);

//        System.out.println("enemy = " + enemy + A.dist(enemy, unit));
        if (enemy == null) {
            return null;
        }

        if (!enemy.isEnemy() || !enemy.isAlive() || !enemy.isVisible()) {
            System.err.println(enemy + ", enemy:" + enemy.isEnemy() + ", alive:" + enemy.isAlive() + ", visible:" + enemy.isVisible());
            throw new RuntimeException("This is crazy, it should never happen, but with wrong logic it can happen.");
        }

//        return enemy;
        return selectWeakestEnemyInRangeOfType(enemy.type(), enemy, unit);
    }

    // =========================================================

    private static AUnit selectUnitToAttackByType(AUnit unit, double maxDistFromEnemy) {
        if (maxDistFromEnemy > 1000) {
            maxDistFromEnemy = 15;
        }

        // Quit early if no target at all
        if (Select.enemyRealUnits(true)
                .effVisible()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, false, true)
                .count() == 0) {
            return null;
        }

        // =========================================================

        AUnit target;
        enemyBuildings = Select.enemy()
                .effVisible()
                .buildings()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, false, true);
        enemyUnits = Select.enemyRealUnits(false)
                .effVisible()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, false, true);

        // =========================================================

        if ((target = ATargetingForSpecificUnits.target(unit)) != null) {
            if (AEnemyTargeting.DEBUG) System.out.println("A = "+ target);
            return target;
        }

        // === Crucial units =======================================

        if ((target = ATargetingCrucial.target(unit)) != null) {
//            if (!target.type().isCarrier()) {
//                System.out.println(A.now() + "  #" + unit.getID() + " " + unit.shortName() + " > " + target.shortName());
//            }
            if (AEnemyTargeting.DEBUG) System.out.println("B = "+ target);
            return target;
        }

        // === Important units =====================================

        if ((target = ATargetingImportant.target(unit)) != null) {
            if (AEnemyTargeting.DEBUG) System.out.println("C = "+ target);
            return target;
        }

        // === Standard targets ====================================

        if ((target = ATargetingStandard.target(unit)) != null) {
            if (AEnemyTargeting.DEBUG) System.out.println("D = "+ target);
            return target;
        }

        // =====

        return target;
    }

    // =========================================================

    private static AUnit selectWeakestEnemyInRangeOfType(AUnitType enemyType, AUnit enemy, AUnit ourUnit) {
        Select<AUnit> targets = (Select<AUnit>) Select.enemy()
                .ofType(enemyType)
//        Select<AUnit> targets = (Select<AUnit>) Select.enemies(enemyType)
                .effVisible()
                .canBeAttackedBy(ourUnit, true, true);

//        System.err.println(Select.enemy().size() + " // " + Select.enemy().ofType(enemyType).size());
//        System.err.println(targets.size() + " // " + targets.clone() + " // " + targets.clone().mostWounded());
        AUnit mostWounded = targets.clone().mostWounded();
        if (mostWounded != null && mostWounded.isWounded()) {
            return mostWounded;
        }

        AUnit nearest = targets.clone().nearestTo(ourUnit);
        if (nearest != null) {
            return nearest;
        }

//        System.err.println("Shouldnt reach here, return default enemy");

        return enemy;
    }
    
}
