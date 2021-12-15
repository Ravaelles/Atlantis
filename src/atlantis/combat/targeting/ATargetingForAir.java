package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.Enemy;

public class ATargetingForAir {

    protected static AUnit targetForAirUnits(AUnit unit) {
        AUnit target;

        if ((target = targetInShootingRange(unit)) != null) {
            return target;
        }

        if ((target = targetOutsideShootingRange(unit)) != null) {
            return target;
        }

        return target;
    }

    // =========================================================

    private static AUnit targetInShootingRange(AUnit unit) {
        AUnit target;

        // =========================================================
        // Target WORKERS

        target = AEnemyTargeting.enemyUnits.clone()
                .workers()
                .inShootRangeOf(unit)
                .mostWounded();
        if (target != null) {
            return target;
        }

        return null;
    }

    private static AUnit targetOutsideShootingRange(AUnit unit) {
        AUnit target;

        // =========================================================
        // Target WORKERS

        target = AEnemyTargeting.enemyUnits.clone()
                .workers()
                .inRadius(10, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target AIR units

        if (Enemy.zerg()) {
            target = AEnemyTargeting.enemyUnits.clone()
                    .air()
//                    .ofType(AUnitType.Zerg_Overlord)
                    .inShootRangeOf(unit)
                    .nearestTo(unit);
            if (target != null) {
                return target;
            }
        }

        // =========================================================
        // Target DEFENSIVE BUILDINGS

        if (Enemy.zerg()) {
            target = AEnemyTargeting.enemyUnits.clone()
                    .ofType(AUnitType.Zerg_Sunken_Colony)
                    .inRadius(15, unit)
                    .nearestTo(unit);
            if (target != null) {
                return target;
            }
        }

        return null;
    }

}
