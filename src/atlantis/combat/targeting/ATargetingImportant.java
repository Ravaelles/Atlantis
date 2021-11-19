package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ATargetingImportant extends AEnemyTargeting {

    public static AUnit target(AUnit unit) {

        // =========================================================
        // =========================================================
        // =========== REMEMBER, AT THIS POINT =====================
        // ======== ENEMY IS AT MOST 15 TILES AWAY =================
        // =========================================================
        // =========================================================

        AUnit target;

        if (unit.isAirUnit() && (target = ATargetingForAir.targetForAirUnits(unit)) != null) {
            return target;
        }

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
        // Target AIR UNITS INRANGE

        target = enemyUnits.clone()
                .air()
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            if (AEnemyTargeting.debug(unit)) System.out.println("C1 = " + target);
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
                .canBeAttackedBy(unit, 1)
                .nearestTo(unit);
        if (target != null) {
            return defensiveBuildingOrScvRepairingIt(unit);
        }

        target = Select.enemy()
                .ofType(
                        AUnitType.Protoss_Photon_Cannon,
                        AUnitType.Terran_Bunker,
                        AUnitType.Zerg_Sunken_Colony
                )
                .inRadius(11, unit)
                .nearestTo(unit);
        if (target != null) {
            return defensiveBuildingOrScvRepairingIt(unit);
        }

        // =========================================================
        // Target COMBAT UNITS IN RANGE

        // Ignore MEDICS
        target = enemyUnits.clone()
                .combatUnits()
                .excludeTypes(AUnitType.Terran_Medic)
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            if (AEnemyTargeting.debug(unit)) System.out.println("C1 = " + target);
            return target;
        }

        // Take into account excluded units above e.g. MEDICS
        target = enemyUnits.clone()
                .ofType(AUnitType.Terran_Medic)
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            if (AEnemyTargeting.debug(unit)) System.out.println("C1 = " + target);
            return target;
        }

        return null;
    }

    private static AUnit defensiveBuildingOrScvRepairingIt(AUnit unit) {
        if (!unit.isBunker()) {
            return unit;
        }

        // Target repairers
        AUnit repairer = Select.enemy().workers().notGathering().inRadius(2, unit)
                .canBeAttackedBy(unit, 1.7).nearestTo(unit);
        if (repairer != null) {
            return repairer;
        }

        return unit;
    }

    private static AUnit targetOutsideShootingRange(AUnit unit) {
        AUnit target;

        // =========================================================
        // Target COMBAT UNITS IN RANGE

        target = enemyUnits.clone()
                .combatUnits()
                .inRadius(13, unit)
                .nearestTo(unit);
        if (target != null) {
            if (AEnemyTargeting.debug(unit)) System.out.println("C4 = " + target);
            return target;
        }

        // =========================================================
        // Including unfinished defensive buildings

        target = Select.enemy()
                .ofType(
                        AUnitType.Protoss_Photon_Cannon,
                        AUnitType.Zerg_Sunken_Colony,
                        AUnitType.Zerg_Creep_Colony,
                        AUnitType.Zerg_Spore_Colony,
                        AUnitType.Terran_Bunker,
                        AUnitType.Terran_Missile_Turret
                )
                .inRadius(12, unit)
                .canBeAttackedBy(unit, 4)
                .nearestTo(unit);

//        if (AEnemyTargeting.debug(unit)) System.out.println("C5 = " + target);
        return target;
    }

}
