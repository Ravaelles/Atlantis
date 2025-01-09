package atlantis.combat.targeting.generic;

import atlantis.combat.targeting.generic.protoss.ATargetingAsDragoon;
import atlantis.combat.targeting.generic.protoss.ATargetingAsZealot;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ATargetingImportant extends ATargeting {
    public ATargetingImportant(AUnit unit) {
        super(unit);
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

        // === As Protoss ==========================================

        if (unit.isProtoss()) {
            target = ATargetingAsDragoon.target(unit, enemyUnits);
            if (target != null) {
//                System.out.println("ImpGoon = " + target + " / " + target.hp());
//                debug("ImpGoon = " + target);
                return target;
            }

            target = ATargetingAsZealot.target(unit, enemyUnits);
            if (target != null) {
//                System.out.println("ImpZealot = " + target + " / " + target.hp());
                debug("ImpZealot = " + target);
                return target;
            }
        }

        // =========================================================
        // Close Zerglings/Zealots/Firebats IN RANGE

        target = enemyUnits
            .ofType(
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Protoss_Zealot,
                AUnitType.Protoss_Dragoon,
                AUnitType.Terran_Bunker,
                AUnitType.Terran_Firebat,
                AUnitType.Terran_Marine,
                AUnitType.Zerg_Sunken_Colony,
                AUnitType.Zerg_Lurker,
                AUnitType.Zerg_Zergling,
                AUnitType.Zerg_Hydralisk
            )
            .inRadius(6.5, unit)
            .canBeAttackedBy(unit, 0)
            .mostWoundedOrNearest(unit);
        if (target != null) {
            debug("CombatUnit_inRange = " + target);
            return target;
        }

        // === CB in range ===========================================

        target = enemyBuildings
            .ofType(
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Terran_Bunker,
                AUnitType.Zerg_Sunken_Colony,
                AUnitType.Zerg_Spore_Colony
            )
            .canBeAttackedBy(unit, 0)
            .nearestTo(unit);
        if (target != null) {
            debug("Targ_Close_CBs = " + target);
            return target;
        }

        // =========================================================
        // Defensive buildings ALMOST IN RANGE

        Selection importantBuildings = enemyBuildings
            .ofType(
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Terran_Bunker,
                AUnitType.Zerg_Sunken_Colony
            )
            .canBeAttackedBy(unit, unit.isMelee() ? 3.95 : 2.8);
//            .inRadius(AUnit.NEAR_DIST, unit);

        target = importantBuildings.mostWoundedOrNearest(unit);
//        System.err.println("target = " + target + " / " + target.hp());

        if (target != null && target.isHealthy()) target = importantBuildings.mostWoundedOrNearest(unit.squadLeader());
        if (target != null) {
            debug("Targ_CBs_A = " + target);
            return getThisCombatBuildingOrScvRepairingIt(target);
        }

//        // === Creep in range ===========================================
//
//        target = enemyBuildings
//            .ofType(AUnitType.Zerg_Creep_Colony)
//            .canBeAttackedBy(unit, 0)
//            .mostWoundedOrNearest(unit);
//        if (target != null) {
//            debug("Targ_Close_Creeps = " + target);
//            return target;
//        }

        // =========================================================

        target = enemyUnits
            .ofType(
                AUnitType.Zerg_Devourer
            )
//                .inShootRangeOf(unit)
            .inRadius(AUnit.NEAR_DIST, unit)
            .mostWounded();
        if (target != null) {
            debug("Zz20 = " + target);
            return target;
        }

        target = enemyUnits
            .ofType(
                AUnitType.Zerg_Mutalisk
            )
//                .inShootRangeOf(unit)
            .inRadius(AUnit.NEAR_DIST, unit)
            .mostWoundedOrNearest(unit);
        if (target != null) {
            debug("Zz30 = " + target);
            return target;
        }

        // =========================================================
        // Target COMBAT UNITS IN RANGE

        // Ignore MEDICS
        target = enemyUnits
            .combatUnits()
            .excludeTypes(AUnitType.Terran_Medic)
            .inShootRangeOf(unit)
//            .nearestTo(unit);
            .mostWoundedOrNearest(unit);
        if (target != null) {
            debug("C1b = " + target);
            return target;
        }

        // Take into account excluded units above e.g. MEDICS
        target = enemyUnits
            .ofType(AUnitType.Terran_Medic)
            .inShootRangeOf(unit)
//            .nearestTo(unit);
            .mostWoundedOrNearest(unit);
        if (target != null) {
            debug("C1c = " + target);
            return target;
        }

//        // Ignore MEDICS and ZEALOTS
//        target = enemyUnits
//            .combatUnits()
//            .excludeTypes(AUnitType.Terran_Medic, AUnitType.Protoss_Zealot)
//            .inShootRangeOf(unit)
//            .nearestTo(unit);
//        if (target != null) {
//            debug("C1b = " + target);
//            return target;
//        }
//
//        // Take into account excluded units above e.g. MEDICS
//        target = enemyUnits
//            .ofType(AUnitType.Terran_Medic, AUnitType.Protoss_Zealot)
//            .inShootRangeOf(unit)
//            .nearestTo(unit);
//        if (target != null) {
//            debug("C1c = " + target);
//            return target;
//        }

        // === Any CB ===========================================

        target = enemyBuildings
            .ofType(
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Terran_Bunker,
                AUnitType.Zerg_Sunken_Colony,
                AUnitType.Zerg_Spore_Colony
            )
            .inRadius(AUnit.NEAR_DIST, unit)
            .canBeAttackedBy(unit, unit.isMelee() ? 5 : 2.8)
            .mostWoundedOrNearest(unit);
        if (target != null) {
            debug("Targ_CBs_B = " + target);
            return target;
        }

        // === Very close enemy workers, probably attacking us ===

        target = enemyUnits
            .workers()
            .inRadius(unit.isRanged() ? 1.6 : 1.15, unit)
            .mostWoundedOrNearest(unit);
        if (target != null) {
            return target;
        }

        // =========================================================

        return null;
    }

    private AUnit getThisCombatBuildingOrScvRepairingIt(AUnit target) {
        if (!target.isBunker()) {
            debug("C0c = " + target);
            return target;
        }

        // Target repairers
        AUnit repairer = enemyUnits.workers()
            .repairing()
            .inRadius(2, target)
            .canBeAttackedBy(target, 2.1)
            .nearestTo(target);
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
        // Enemy units in range

        if (unit.isMelee()) {
            Selection enemiesInRange = enemyUnits.excludeMedics().inShootRangeOf(unit);
            target = enemiesInRange.mostWounded();
            if (target != null && (target.isWounded() || unit.cooldown() <= 3)) {
                debug("C3_wnd_in = " + target);
                return target;
            }

//            double outBonus = unit.meleeEnemiesNearCount(1.5) <= 2 ? 2 : 0.8;
            double outBonus = 1.4;
            enemiesInRange = enemyUnits.excludeMedics().canBeAttackedBy(unit, outBonus);
            target = enemiesInRange.mostWounded();
            if (target != null && target.isWounded()) {
                debug("C3_wnd_out = " + target);
                return target;
            }

            target = leaderEnemies().excludeMedics().nearestTo(unit);
            if (target != null) {
                debug("C3_near = " + target);
                return target;
            }
        }

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

        // === Very close enemy workers ============================

        if (unit.isWounded() || unit.lastUnderAttackLessThanAgo(30)) {
            target = enemyUnits
                .workers()
                .inRadius(1.04, unit)
                .mostWoundedOrNearest(unit);
            if (target != null) {
                debug("Target_Offensive_Workers = " + target);
                return target;
            }
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

        if (target != null) {
            debug("C = " + target);
            return target;
        }

        // === Unfinished combat building ===================================

        Selection enemyCbs = Select.enemy().buildings()
            .ofType(
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Terran_Bunker,
                AUnitType.Zerg_Sunken_Colony
            );

        target = enemyCbs
            .inRadius(6, unit)
            .mostWounded();

        if (target != null) {
            debug("C5x = " + target);
            return target;
        }

        target = enemyCbs
            .inRadius(7, unit)
            .nearestTo(unit);

        if (target != null) {
            debug("C5y = " + target);
            return target;
        }

        // =========================================================
        // Including unfinished defensive buildings

        target = enemyBuildings
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
            debug("C5z = " + target);
            return target;
        }

        // === Creep colonies ======================================

        target = enemyUnits
            .ofType(
                AUnitType.Zerg_Creep_Colony
            )
            .inRadius(8, unit)
            .canBeAttackedBy(unit, unit.isMelee() ? 3.7 : 1.7)
            .mostWoundedOrNearest(unit);
        if (target != null) {
            debug("Targ_CB_CreepC = " + target);
            return target;
        }

        // =========================================================
        // Including unfinished defensive buildings

        target = enemyBuildings
            .ofType(
                AUnitType.Zerg_Creep_Colony
            )
            .inRadius(7, unit)
            .mostWounded();

        if (target != null) {
            debug("C5d = " + target);
            return target;
        }

        // === WORKERS in RANGE ======================================================

        target = enemyUnits
            .workers()
            .inRadius(unit.isMelee() ? 1 : 3.6, unit)
            .nearestTo(unit);

        if (target != null) {
            debug("C5bWork = " + target);
            return target;
        }

        // =========================================================
        // Close Zerglings/Zealots/Firebats OUT OF RANGE

        target = enemyUnits
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
            .canBeAttackedBy(unit, 2)
            .nearestTo(unit);

        if (target != null) {
            debug("C5a = " + target);
            return target;
        }

        // === BASES NEARBY ======================================================

        target = enemyBuildings.bases()
            .inRadius(unit.isMelee() ? 3 : 8, unit)
            .nearestTo(unit);

        if (target != null) {
            debug("C5b0 = " + target);
            return target;
        }

        // === WORKERS ======================================================

        target = enemyUnits
            .workers()
            .inRadius(unit.isMelee() ? 4 : 7, unit)
            .nearestTo(unit);

        if (target != null) {
            debug("C5b1 = " + target);
            return target;
        }

        // === Damaged bases ======================================================

        target = enemyBuildings
            .bases()
            .notHavingHp(140)
            .inRadius(AUnit.NEAR_DIST, unit)
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
            .inRadius(AUnit.NEAR_DIST, unit)
            .nearestTo(unit);

        if (target != null) {
            debug("D6b = " + target);
            return target;
        }

        // =========================================================

        return target;
    }

}
