package atlantis.combat.targeting;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.select.Selection;

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
                .canBeAttackedBy(unit, 8)
                .count() == 0) {
            return null;
        }

        // =========================================================

        AUnit target;
        enemyBuildings = Select.enemy()
                .effVisible()
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
        Selection targets = Select.enemy()
                .ofType(enemyType)
                .effVisible()
                .canBeAttackedBy(ourUnit, 0);

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
