package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ATargetingImportant extends ATargeting {

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

        // =========================================================
        // Target AIR UNITS IN RANGE

        target = enemyUnits
                .air()
                .excludeTypes(AUnitType.Zerg_Overlord)
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("C1a = " + target);
            return target;
        }

        // =========================================================
        // Defensive buildings IN RANGE

        target = Select.enemy()
                .ofType(
                        AUnitType.Protoss_Photon_Cannon,
                        AUnitType.Terran_Bunker,
                        AUnitType.Zerg_Sunken_Colony
                )
                .canBeAttackedBy(unit, 0)
                .mostWounded();
        if (target != null) {
            return combatBuildingOrScvRepairingIt(target);
        }

        target = enemyUnits
                .ofType(
                        AUnitType.Protoss_Photon_Cannon,
                        AUnitType.Terran_Bunker,
                        AUnitType.Zerg_Sunken_Colony
                )
                .canBeAttackedBy(unit, 5)
                .nearestTo(unit);
        if (target != null) {
            return combatBuildingOrScvRepairingIt(target);
        }

        // =========================================================

        target = enemyUnits
            .ofType(
                AUnitType.Zerg_Devourer
            )
//                .inShootRangeOf(unit)
            .inRadius(15, unit)
            .mostWounded();
        if (target != null) {
            return target;
        }

        target = enemyUnits
            .ofType(
                AUnitType.Zerg_Mutalisk
            )
//                .inShootRangeOf(unit)
            .inRadius(15, unit)
            .mostWounded();
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target COMBAT UNITS IN RANGE

        // Ignore MEDICS
        target = enemyUnits
                .combatUnits()
                .excludeTypes(AUnitType.Terran_Medic)
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("C1b = " + target);
            return target;
        }

        // Take into account excluded units above e.g. MEDICS
        target = enemyUnits
                .ofType(AUnitType.Terran_Medic)
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("C1c = " + target);
            return target;
        }

        return null;
    }

    private static AUnit combatBuildingOrScvRepairingIt(AUnit unit) {
        if (!unit.isBunker()) {
            if (ATargeting.DEBUG) System.out.println("C0c = " + unit);
            return unit;
        }

        // Target repairers
        AUnit repairer = Select.enemy().workers().notGathering().inRadius(2, unit)
                .canBeAttackedBy(unit, 1.7).nearestTo(unit);
        if (repairer != null) {
            if (ATargeting.DEBUG) System.out.println("C0a = " + repairer);
            return repairer;
        }

        if (ATargeting.DEBUG) System.out.println("C0b = " + unit);
        return unit;
    }

    private static AUnit targetOutsideShootingRange(AUnit unit) {
        AUnit target;

        // =========================================================
        // Target COMBAT UNITS IN RANGE

        target = enemyUnits
                .combatUnits()
                .excludeMedics()
//                .inShootRangeOf(unit)
                .inRadius(7, unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("C4 = " + target);
            return target;
        }

        // =========================================================
        // Including unfinished defensive buildings

        target = Select.enemy()
                .ofType(
                        AUnitType.Protoss_Photon_Cannon,
                        AUnitType.Zerg_Sunken_Colony,
//                        AUnitType.Zerg_Creep_Colony,
                        AUnitType.Zerg_Spore_Colony,
                        AUnitType.Terran_Bunker,
                        AUnitType.Terran_Missile_Turret
                )
                .inRadius(14, unit)
                .canBeAttackedBy(unit, 0)
                .mostWounded();

        if (target != null && ATargeting.DEBUG) System.out.println("C5 = " + target);

        // === WORKERS ======================================================

        target = enemyUnits
            .workers()
            .inRadius(unit.isMelee() ? 8 : 12, unit)
            .nearestTo(unit);

        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("C5b = " + target);
            return target;
        }

        // === Damaged bases ======================================================

        target = enemyBuildings
            .bases()
            .notHavingHp(140)
            .inRadius(15, unit)
            .mostWounded();
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("C6 = " + target);
            return target;
        }

        // =========================================================
        // Stategic buildings worth destroying

        target = enemyBuildings
            .ofType(
                AUnitType.Protoss_Fleet_Beacon,
                AUnitType.Protoss_Templar_Archives,
                AUnitType.Terran_Armory,
//                AUnitType.Terran_Engineering_Bay,
//                AUnitType.Terran_Academy,
                AUnitType.Zerg_Spawning_Pool,
                AUnitType.Zerg_Spire,
                AUnitType.Zerg_Greater_Spire
            )
            .inRadius(15, unit)
            .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("D6b = " + target);
            return target;
        }

        // =========================================================

        return target;
    }

}
