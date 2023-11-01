package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ATargetingCrucial extends ATargeting {

    public ATargetingCrucial(AUnit unit, Selection enemyUnits, Selection enemyBuildings) {
        super(unit, enemyUnits, enemyBuildings);
    }

    public AUnit target() {

        // =========================================================
        // =========================================================
        // =========== REMEMBER, AT THIS POINT =====================
        // ======== ENEMY IS AT MOST 15 TILES AWAY =================
        // =========================================================
        // =========================================================

        AUnit target;

        if ((target = targetInShootingRange()) != null) {
            return target;
        }

        if ((target = targetOutsideShootingRange()) != null) {
            return target;
        }

        return null;
    }

    // =========================================================

    private AUnit targetInShootingRange() {
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
                debug("CR1 = " + target);
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
            debug("CR2 = " + target);
            return target;
        }

        // =========================================================
        // DEFILER / LURKER in range

        target = enemyUnits
            .ofType(AUnitType.Zerg_Defiler)
            .effVisible()
            .inRadius(13, unit)
            .nearestTo(unit);
        if (target != null) {
            debug("CR3 = " + target);
            return target;
        }

        target = enemyUnits
            .ofType(AUnitType.Zerg_Lurker, AUnitType.Terran_Ghost)
            .inShootRangeOf(unit)
            .nearestTo(unit);
        if (target != null) {
            debug("CR3 = " + target);
            return target;
        }

        target = enemyUnits
            .ofType(AUnitType.Zerg_Lurker)
            .inRadius(10, unit)
            .nearestTo(unit);
        if (target != null) {
            debug("CR4 = " + target);
            return target;
        }

        // =========================================================
        // HIGH TEMPLARS

        target = enemyUnits
            .ofType(AUnitType.Protoss_High_Templar)
            .canBeAttackedBy(unit, 1)
            .nearestTo(unit);

        if (target != null) {
            debug("CR5 = " + target);
            return target;
        }

        // =========================================================
        // MELEE should attack CLOSE targets if too clustered

        if (unit.isMelee()) {
            Selection NearEnemies = enemyUnits.inRadius(0.9, unit);
            if (NearEnemies.atLeast(2)) {
                debug("CR6 = " + target);
                return NearEnemies.mostWounded();
            }
        }

        return null;
    }

    private AUnit targetOutsideShootingRange() {
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
                debug("CR7 = " + target);
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
            debug("CR8 = " + target);
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
            debug("CR9 = " + target);
            return target;
        }

        // =========================================================
        // DEADLY, but ALLOW SMALL OUT OF RANGE bonus

        target = enemyUnits
            .ofType(
                AUnitType.Protoss_Reaver,
                AUnitType.Terran_Science_Vessel
            )
            .inRadius(groundRange + 2, unit)
            .nearestTo(unit);
        if (target != null) {
            debug("CR10 = " + target);
            return target;
        }

        // =========================================================
        // DEADLY

        target = enemyUnits
            .ofType(
                AUnitType.Protoss_Archon
            )
            .inRadius(groundRange + 2.2, unit)
            .nearestTo(unit);
        if (target != null) {
            debug("CR10b = " + target);
            return target;
        }

        // =========================================================
        // DEADLY units, but can wait to BE IN RANGE

        target = enemyUnits
            .ofType(
                AUnitType.Protoss_Dark_Templar,
                AUnitType.Zerg_Scourge,
                AUnitType.Zerg_Defiler,
                AUnitType.Zerg_Guardian
            )
            .inShootRangeOf(unit)
            .nearestTo(unit);
        if (target != null) {
            debug("CR11 = " + target);
            return target;
        }

        // =========================================================
        // Special case - SHUTTLE

        if ((target = (new ATransportTargeting(unit)).target()) != null) {
            debug("CR12 = " + target);
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
            debug("CR13 = " + target);
            return target;
        }

        return target;
    }

    public static boolean isCrucialUnit(AUnit target) {
        if (target == null) return false;

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
