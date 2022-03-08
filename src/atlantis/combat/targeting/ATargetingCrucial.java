package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ATargetingCrucial extends ATargeting {

    public static AUnit target(AUnit unit) {

        // =========================================================
        // =========================================================
        // =========== REMEMBER, AT THIS POINT =====================
        // ======== ENEMY IS AT MOST 15 TILES AWAY =================
        // =========================================================
        // =========================================================

        AUnit target;

        if ((target = targetInShootingRange(unit)) != null) {
            return target;
        }

        if ((target = targetOutsideShootingRange(unit)) != null) {
            return target;
        }

        return null;
    }

    // =========================================================

    private static AUnit targetInShootingRange(AUnit unit) {
        AUnit target;

        double groundRange = unit.groundWeaponRange();
//        double airRange = unit.getWeaponRangeAir();

        // =========================================================
        // Attack MINES

        if (unit.isRanged()) {
            target = enemyUnits
                    .ofType(AUnitType.Terran_Vulture_Spider_Mine)
                    .inShootRangeOf(groundRange + 4, unit)
                    .randomWithSeed(700 + unit.id());
            if (target != null) {
                if (ATargeting.DEBUG) System.out.println("CR1 = " + target);
                return target;
            }
        }

        // =========================================================
        // Targetable OBSERVERS near CARRIERS

        target = enemyUnits
                .ofType(AUnitType.Protoss_Observer)
                .effVisible()
                .inRadius(unit.isAir() ? 40 : 13, unit)
                .mostWounded();
        if (target != null && Select.enemies(AUnitType.Protoss_Carrier).inRadius(15, target).atLeast(1)) {
            if (ATargeting.DEBUG) System.out.println("CR2 = " + target);
            return target;
        }

        // =========================================================
        // DEFILER / LURKER in range

        target = enemyUnits
                .ofType(AUnitType.Zerg_Defiler)
                .effVisible()
                .inRadius(15, unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("CR3 = " + target);
            return target;
        }

        target = enemyUnits
                .ofType(AUnitType.Zerg_Lurker)
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("CR3 = " + target);
            return target;
        }

        target = enemyUnits
                .ofType(AUnitType.Zerg_Lurker)
                .inRadius(10, unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("CR4 = " + target);
            return target;
        }

        // =========================================================
        // HIGH TEMPLARS

        target = enemyUnits
                .ofType(AUnitType.Protoss_High_Templar)
                .inRadius(8, unit)
                .nearestTo(unit);
//        System.out.println("target = " + target + " // " + (target != null ? unit.distTo(target) : ""));
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("CR5 = " + target);
            return target;
        }

        // =========================================================
        // MELEE should attack CLOSE targets if too clustered

        if (unit.isMelee()) {
            Selection NearEnemies = enemyUnits.inRadius(0.9, unit);
            if (NearEnemies.atLeast(2)) {
                if (ATargeting.DEBUG) System.out.println("CR6 = " + target);
                return NearEnemies.mostWounded();
            }
        }

        return null;
    }

    private static AUnit targetOutsideShootingRange(AUnit unit) {
        AUnit target;
        double groundRange = unit.groundWeaponRange();

        // =========================================================
        // DEADLIEST shit out there,
        // Move to attack it WAY NOT IN RANGE

        if (unit.isRanged()) {
            target = enemyUnits
                    .ofType(
                            AUnitType.Protoss_Observer,
                            AUnitType.Terran_Siege_Tank_Siege_Mode
                    )
    //                .inShootRangeOf(unit)
                    .inRadius(5, unit)
                    .nearestTo(unit);
            if (target != null) {
                if (ATargeting.DEBUG) System.out.println("CR7 = " + target);
                return target;
            }
        }

        target = enemyUnits
                .ofType(
                        AUnitType.Protoss_Reaver,
                        AUnitType.Terran_Siege_Tank_Tank_Mode,
                        AUnitType.Terran_Siege_Tank_Siege_Mode
                )
                .inRadius(9, unit)
                .randomWithSeed(unit.id());
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("CR8 = " + target);
            return target;
        }

        target = enemyUnits
                .ofType(
                        AUnitType.Zerg_Defiler,
                        AUnitType.Protoss_Carrier,
                        AUnitType.Terran_Siege_Tank_Tank_Mode,
                        AUnitType.Terran_Siege_Tank_Siege_Mode
                )
                .inRadius(8, unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("CR9 = " + target);
            return target;
        }

        // =========================================================
        // DEADLY, but ALLOW SMALL OUT OF RANGE bonus

        target = enemyUnits
                .ofType(
                        AUnitType.Protoss_High_Templar,
                        AUnitType.Protoss_Reaver,
                        AUnitType.Terran_Science_Vessel
                )
                .inRadius(groundRange + 2, unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("CR10 = " + target);
            return target;
        }

        // =========================================================
        // DEADLY

        target = enemyUnits
                .ofType(
                        AUnitType.Protoss_Archon
                )
                .inRadius(groundRange + 1, unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("CR10b = " + target);
            return target;
        }

        // =========================================================
        // DEADLY units, but can wait to BE IN RANGE

        target = enemyUnits
                .ofType(
                        AUnitType.Protoss_Dark_Templar,
                        AUnitType.Zerg_Scourge,
                        AUnitType.Zerg_Defiler,
                        AUnitType.Zerg_Mutalisk
                )
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("CR11 = " + target);
            return target;
        }

        // =========================================================
        // Special case - SHUTTLE

        if ((target = ATransportTargeting.target(unit)) != null) {
            if (ATargeting.DEBUG) System.out.println("CR12 = " + target);
            return target;
        }

        // =========================================================
        // DEADLY but attack in last order

        target = enemyUnits
                .ofType(
                        AUnitType.Protoss_Archon,
                        AUnitType.Protoss_Observer,
                        AUnitType.Zerg_Ultralisk
                )
                .inRadius(groundRange + 0.7, unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("CR13 = " + target);
            return target;
        }

        return target;
    }

    public static boolean isCrucialUnit(AUnit target) {
        if (target == null) {
            return false;
        }

        if (target.is(AUnitType.Protoss_Carrier)) {
            return Select.enemies(AUnitType.Protoss_Observer).inRadius(12, target).isEmpty();
        }

        return target.is(
                AUnitType.Protoss_Observer,
                AUnitType.Protoss_Reaver,
                AUnitType.Protoss_Dark_Templar,
                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Zerg_Defiler
        );
    }
}
