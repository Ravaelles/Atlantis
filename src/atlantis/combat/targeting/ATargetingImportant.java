package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ATargetingImportant extends ATargeting {

    public ATargetingImportant(AUnit unit, Selection enemyUnits, Selection enemyBuildings) {
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

        // =========================================================
        // Target AIR UNITS IN RANGE

        target = enemyUnits
            .air()
//            .excludeTypes(AUnitType.Protoss_Observer, AUnitType.Terran_Wraith, AUnitType.Zerg_Queen)
            .excludeOverlords()
            .inShootRangeOf(unit)
            .nearestTo(unit);
        if (target != null) {
            debug("C1a = " + target);
            return target;
        }

        target = enemyUnits
            .air()
            .excludeOverlords()
            .inShootRangeOf(unit)
            .nearestTo(unit);
        if (target != null) {
            debug("C1a = " + target);
            return target;
        }

        // =========================================================
        // Close Zerglings/Zealots/Firebats IN RANGE

        target = Select.enemy()
            .ofType(
                AUnitType.Protoss_Zealot,
                AUnitType.Terran_Firebat,
                AUnitType.Terran_Marine,
                AUnitType.Zerg_Zergling
            )
            .inRadius(5, unit)
            .canBeAttackedBy(unit, 0)
            .mostWounded();
        if (target != null) return target;

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
            return getThisCombatBuildingOrScvRepairingIt(target);
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
            return getThisCombatBuildingOrScvRepairingIt(target);
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
            debug("C1b = " + target);
            return target;
        }

        // Take into account excluded units above e.g. MEDICS
        target = enemyUnits
            .ofType(AUnitType.Terran_Medic)
            .inShootRangeOf(unit)
            .nearestTo(unit);
        if (target != null) {
            debug("C1c = " + target);
            return target;
        }

        return null;
    }

    private AUnit getThisCombatBuildingOrScvRepairingIt(AUnit target) {
        if (!target.isBunker()) {
            debug("C0c = " + target);
            return target;
        }

        // Target repairers
        AUnit repairer = Select.enemy().workers().notGathering().inRadius(2, target)
            .canBeAttackedBy(target, 1.7).nearestTo(target);
        if (repairer != null) {
            debug("C0a = " + repairer);
            return repairer;
        }

        debug("C0b = " + target);
        return target;
    }

    private AUnit targetOutsideShootingRange() {
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
            debug("C4 = " + target);
            return target;
        }

        // =========================================================
        // Including unfinished defensive buildings

        target = enemyBuildings
            .onlyCompleted()
            .ofType(
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Zerg_Sunken_Colony,
//                        AUnitType.Zerg_Creep_Colony,
                AUnitType.Zerg_Spore_Colony,
                AUnitType.Terran_Bunker,
                AUnitType.Terran_Missile_Turret
            )
            .inRadius(9, unit)
            .canBeAttackedBy(unit, 999)
            .mostWounded();

        debug("C5 = " + target);


        // =========================================================
        // Close Zerglings/Zealots/Firebats OUT OF RANGE

        target = Select.enemy()
            .ofType(
                AUnitType.Protoss_Probe,
                AUnitType.Protoss_Zealot,
                AUnitType.Terran_SCV,
                AUnitType.Terran_Firebat,
                AUnitType.Terran_Marine,
                AUnitType.Zerg_Drone,
                AUnitType.Zerg_Zergling
            )
            .inRadius(9, unit)
            .canBeAttackedBy(unit, 999)
            .nearestTo(unit);
        if (target != null) return target;

        // === WORKERS ======================================================

        target = enemyUnits
            .workers()
            .inRadius(unit.isMelee() ? 8 : 15, unit)
            .nearestTo(unit);

        if (target != null) {
            debug("C5b = " + target);
            return target;
        }

        // =========================================================
        // Including unfinished defensive buildings

        target = unit.enemiesNear()
            .ofType(
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Zerg_Sunken_Colony,
//                        AUnitType.Zerg_Creep_Colony,
                AUnitType.Zerg_Spore_Colony,
                AUnitType.Terran_Bunker,
                AUnitType.Terran_Missile_Turret
            )
            .inRadius(14, unit)
//            .canBeAttackedBy(unit, 6)
            .mostWounded();

        if (target != null) {
            debug("C5c = " + target);
            return target;
        }

        // === Damaged bases ======================================================

        target = enemyBuildings
            .bases()
            .notHavingHp(140)
            .inRadius(15, unit)
            .mostWounded();

        if (target != null) {
            debug("C6 = " + target);
            return target;
        }

        // =========================================================
        // Stategic buildings worth destroying

        target = enemyBuildings
            .ofType(
                AUnitType.Protoss_Fleet_Beacon,
//                AUnitType.Protoss_Templar_Archives,
//                AUnitType.Terran_Armory,
//                AUnitType.Terran_Engineering_Bay,
//                AUnitType.Terran_Academy,
//                AUnitType.Zerg_Spawning_Pool,
                AUnitType.Zerg_Spire,
                AUnitType.Zerg_Greater_Spire
            )
            .inRadius(15, unit)
            .nearestTo(unit);

        if (target != null) {
            debug("D6b = " + target);
            return target;
        }

        // =========================================================

        return target;
    }

}
