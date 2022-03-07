package atlantis.combat.targeting;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

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
        // Target AIR units

        target = ATargeting.enemyUnits
                .air()
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target REAVERS

        target = ATargeting.enemyUnits
                .ofType(AUnitType.Protoss_Reaver)
                .inRadius(10, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target TRANSPORT

        target = ATargeting.enemyUnits
                .transports(true)
                .inRadius(10, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target WORKERS

        target = ATargeting.enemyUnits
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
        // Target DEFENSIVE BUILDINGS

        target = ATargeting.enemyUnits
                .add(EnemyUnits.foggedUnits())
                .ofType(AUnitType.Zerg_Sunken_Colony)
                .removeDuplicates()
                .inRadius(50, unit)
                .nearestTo(unit);
        if (ATargeting.DEBUG) System.out.println("target = " + target + " // " + unit);

        if (target != null) {
            return target;
        }

        // =========================================================
        // Target WORKERS

        target = ATargeting.enemyUnits
                .workers()
                .inRadius(30, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        return null;
    }

}
