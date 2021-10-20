package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class AEnemyTargeting {

    protected static Select<AUnit> buildings;
    protected static Select<AUnit> units;

    /**
     * For given <b>unit</b> it defines the best close range target from enemy units. The target is not
     * necessarily in the shoot range. Will return <i>null</i> if no enemy can is visible.
     */
    public static AUnit defineBestEnemyToAttackFor(AUnit unit, double maxDistFromEnemy) {
        AUnit enemy = selectUnitToAttackByType(unit, maxDistFromEnemy);
        if (enemy == null) {
            return null;
        }

        return selectWeakestEnemyInRangeOfType(enemy.type(), unit);
    }

    // =========================================================

    private static AUnit selectUnitToAttackByType(AUnit unit, double maxDistFromEnemy) {
        if (maxDistFromEnemy > 1000) {
            maxDistFromEnemy = 15;
        }

        // Quit early if no target at all
        if (Select.enemyRealUnits(true)
                .canBeAttackedBy(unit, false, true)
                .inRadius(maxDistFromEnemy, unit)
                .count() == 0) {
            return null;
        }

        // =========================================================

        AUnit target = null;
        buildings = Select.enemy()
                .buildings()
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, false, true);
        units = Select.enemyRealUnits(false)
                .inRadius(maxDistFromEnemy, unit)
                .canBeAttackedBy(unit, false, true);

        // === Crucial units =======================================

        if ((target = ATargetingCrucial.target(unit)) != null) {
//            if (!target.type().isCarrier()) {
//                System.out.println(target + " // " + target.getHP());
//            }
            return target;
        }

        // === Important units =====================================

        if ((target = ATargetingImportant.target(unit)) != null) {
            return target;
        }

        // === Standard targets ====================================

        if ((target = ATargetingStandard.target(unit)) != null) {
            return target;
        }

        // =====

        return target;
    }

    // =========================================================

    private static AUnit selectWeakestEnemyInRangeOfType(AUnitType enemyType, AUnit ourUnit) {
        Select<AUnit> targets = Select.enemies(enemyType)
                .effVisible().canBeAttackedBy(ourUnit, true, true);

        AUnit mostWounded = targets.clone().mostWounded();
        if (mostWounded != null && mostWounded.isWounded()) {
            return mostWounded;
        }

        AUnit nearest = targets.nearestTo(ourUnit);
        if (nearest != null) {
            return nearest;
        }

        return Select.enemies(enemyType).effVisible().nearestTo(ourUnit);
    }
    
}
