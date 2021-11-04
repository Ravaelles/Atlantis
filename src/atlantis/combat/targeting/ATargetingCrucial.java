package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.List;

public class ATargetingCrucial extends AEnemyTargeting {

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
            target = enemyUnits.clone()
                    .ofType(AUnitType.Terran_Vulture_Spider_Mine)
                    .inShootRangeOf(groundRange + 4, unit)
                    .randomWithSeed(700 + unit.getID());
            if (target != null) {
                return target;
            }
        }

        // =========================================================
        // Targetable observers near carriers

        target = enemyUnits.clone()
                .ofType(AUnitType.Protoss_Observer)
                .effVisible()
                .inRadius(unit.isAirUnit() ? 40 : 11, unit)
                .mostWounded();
        if (target != null && Select.enemies(AUnitType.Protoss_Carrier).inRadius(14, target).atLeast(1)) {
            return target;
        }

        // =========================================================
        // MELEE should attack CLOSE targets if too clustered

        if (unit.isMelee()) {
            Selection nearbyEnemies = enemyUnits.clone().inRadius(0.9, unit);
            if (nearbyEnemies.atLeast(2)) {
                return nearbyEnemies.mostWounded();
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
            target = enemyUnits.clone()
                    .ofType(
                            AUnitType.Protoss_Observer,
                            AUnitType.Terran_Siege_Tank_Siege_Mode,
                            AUnitType.Zerg_Defiler
                    )
    //                .inShootRangeOf(unit)
                    .inRadius(5, unit)
                    .nearestTo(unit);
            if (target != null) {
                return target;
            }
        }

        target = enemyUnits.clone()
                .ofType(
                        AUnitType.Protoss_Reaver,
                        AUnitType.Terran_Siege_Tank_Tank_Mode,
                        AUnitType.Terran_Siege_Tank_Siege_Mode
                )
                .inRadius(9, unit)
                .randomWithSeed(unit.getID());
        if (target != null) {
            return target;
        }

        target = enemyUnits.clone()
                .ofType(
                        AUnitType.Zerg_Defiler,
                        AUnitType.Protoss_Carrier,
                        AUnitType.Terran_Siege_Tank_Tank_Mode,
                        AUnitType.Terran_Siege_Tank_Siege_Mode
                )
                .inRadius(8, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // DEADLY, but ALLOW SMALL OUT OF RANGE bonus

        target = enemyUnits.clone()
                .ofType(
                        AUnitType.Protoss_High_Templar,
                        AUnitType.Protoss_Reaver,
                        AUnitType.Terran_Science_Vessel
                )
                .inRadius(groundRange + 2, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // DEADLY units, but can wait to BE IN RANGE

        target = enemyUnits.clone()
                .ofType(
                        AUnitType.Protoss_Dark_Templar,
                        AUnitType.Zerg_Scourge,
                        AUnitType.Zerg_Defiler,
                        AUnitType.Zerg_Mutalisk
                )
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // DEADLY but attack in last order

        target = enemyUnits.clone()
                .ofType(
                        AUnitType.Protoss_Archon,
                        AUnitType.Protoss_Observer,
                        AUnitType.Zerg_Lurker,
                        AUnitType.Zerg_Ultralisk
                )
                .inRadius(groundRange + 0.7, unit)
                .nearestTo(unit);
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
