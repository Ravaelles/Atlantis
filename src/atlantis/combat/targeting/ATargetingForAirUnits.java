package atlantis.combat.targeting;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class ATargetingForAirUnits {

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

        Selection allEnemies = ATargeting.enemyUnits.withEnemyFoggedUnits()
            .removeDuplicates()
            .effVisible()
            .canBeAttackedBy(unit, 20);

        // =========================================================
        // Target CRUCIAL AIR units

        target = allEnemies
                .air()
                .ofType(
                    AUnitType.Protoss_Observer,
                    AUnitType.Protoss_Arbiter,

                    AUnitType.Terran_Science_Vessel,

                    AUnitType.Zerg_Scourge
                )
                .inShootRangeOf(unit)
                .mostWounded();
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target REAVERS + HT + TANKS + DEFILERS

        target = allEnemies
                .ofType(
                    AUnitType.Protoss_Reaver,
                    AUnitType.Protoss_High_Templar,

                    AUnitType.Terran_Siege_Tank_Siege_Mode,
                    AUnitType.Terran_Siege_Tank_Tank_Mode,

                    AUnitType.Zerg_Defiler
                )
//                .inShootRangeOf(unit)
                .inRadius(15, unit)
                .mostWounded();
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target DT + Mutalisks

        target = allEnemies
                .ofType(
                    AUnitType.Protoss_Dark_Templar,
                    AUnitType.Protoss_Archon,

                    AUnitType.Terran_Ghost,

                    AUnitType.Zerg_Mutalisk
                )
//                .inShootRangeOf(unit)
                .inRadius(15, unit)
                .mostWounded();
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target IMPORTANT AIR units

        target = allEnemies
                .air()
                .ofType(
                    AUnitType.Protoss_Carrier,
                    AUnitType.Protoss_Shuttle,

                    AUnitType.Terran_Battlecruiser,
                    AUnitType.Terran_Dropship,

                    AUnitType.Zerg_Guardian,
                    AUnitType.Zerg_Devourer
                )
                .inShootRangeOf(unit)
                .mostWounded();
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target ANY AIR units

        target = allEnemies
                .air()
                .inShootRangeOf(unit)
                .mostWounded();
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target TRANSPORT

//        target = allEnemies
//                .transports(true)
//                .inRadius(10, unit)
//                .nearestTo(unit);
//        if (target != null) {
//            return target;
//        }

        // =========================================================
        // Target WORKERS

        target = allEnemies
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

        Selection allEnemies = ATargeting.enemyUnits.withEnemyFoggedUnits().removeDuplicates();

        // =========================================================
        // Target WORKERS

        target = allEnemies
                .workers()
                .inRadius(50, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target DISTANT BASES, hoping to find workers

        target = allEnemies
                .bases()
                .inRadius(50, unit)
                .nearestTo(unit);
        if (ATargeting.DEBUG) System.out.println("target AA1 = " + target + " // " + unit);

        if (target != null) {
            return target;
        }

        // =========================================================
        // Target DEFENSIVE BUILDINGS

        target = allEnemies
                .ofType(AUnitType.Zerg_Sunken_Colony)
                .inRadius(50, unit)
                .nearestTo(unit);
        if (ATargeting.DEBUG) System.out.println("target AA2 = " + target + " // " + unit);

        if (target != null) {
            return target;
        }

        // =========================================================
        // Target COMBAT UNITS THAT CAN'T SHOOT AT US

        target = allEnemies
            .combatUnits()
                .notHavingAntiAirWeapon()
                .inRadius(50, unit)
                .nearestTo(unit);
        if (ATargeting.DEBUG) System.out.println("target AA3 = " + target + " // " + unit);

        if (target != null) {
            return target;
        }

        // =========================================================
        // Target ANY COMBAT UNITS

//        target = allEnemies
//                .notHavingAntiAirWeapon()
//                .inRadius(50, unit)
//                .nearestTo(unit);
//        if (ATargeting.DEBUG) System.out.println("target AA4 = " + target + " // " + unit);
//
//        if (target != null) {
//            return target;
//        }

        // =========================================================
        // Target ANY COMBAT UNITS

        target = allEnemies
                .combatUnits()
                .inRadius(50, unit)
                .nearestTo(unit);
        if (ATargeting.DEBUG) System.out.println("target AA5 = " + target + " // " + unit);

        if (target != null) {
            return target;
        }

        return null;
    }

}
