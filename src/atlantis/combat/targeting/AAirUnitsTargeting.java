package atlantis.combat.targeting;

import atlantis.combat.targeting.air.AAirUnitAirTargets;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class AAirUnitsTargeting extends HasUnit {
    private final AAirUnitAirTargets aAirUnitAirTargets;
    protected boolean onlyEnemiesInRangeAreAllowed;
    protected Selection possibleTargets;

    public AAirUnitsTargeting(AUnit unit) {
        this(unit, false);
    }

    public AAirUnitsTargeting(AUnit unit, boolean onlyEnemiesInRangeAreAllowed) {
        super(unit);
        this.onlyEnemiesInRangeAreAllowed = onlyEnemiesInRangeAreAllowed;
        this.aAirUnitAirTargets = new AAirUnitAirTargets(unit);

        assert (unit.isAir());

        possibleTargets = definePossibleTargets();
    }

    protected Selection definePossibleTargets() {
        Selection baseEnemies = onlyEnemiesInRangeAreAllowed ?
            Select.enemyRealUnits(true, true, true).canBeAttackedBy(unit, 0)
            : EnemyUnits.discovered().canBeAttackedBy(unit, 25);

        baseEnemies = baseEnemies.excludeTypes(
            AUnitType.Terran_Missile_Turret, AUnitType.Zerg_Spore_Colony, AUnitType.Protoss_Photon_Cannon
        );

        return baseEnemies
            .removeDuplicates()
            .effVisible();
    }

    public AUnit targetForAirUnit() {
        AUnit target;

        // =========================================================

        AUnit enemyWhoBreachedBase = EnemyUnits.enemyWhoBreachedBase();
        if (
            enemyWhoBreachedBase != null
                && unit.canAttackTarget(enemyWhoBreachedBase, false, true)
        ) {
            return enemyWhoBreachedBase;
        }

        // =========================================================

        if ((target = aAirUnitAirTargets.targetsAir(possibleTargets)) != null) return target;
        if ((target = targetsCrucial()) != null) return target;
        if ((target = targetsStandard()) != null) return target;

        return target;
    }

    // =========================================================

    protected AUnit targetsCrucial() {
        AUnit target;

        // =========================================================
        // Target CRUCIAL AIR units

        target = possibleTargets
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

        target = possibleTargets
            .ofType(
                AUnitType.Protoss_Reaver,
                AUnitType.Protoss_High_Templar,

                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Terran_Siege_Tank_Tank_Mode,

                AUnitType.Zerg_Defiler,
                AUnitType.Zerg_Guardian
            )
            .inShootRangeOf(unit)
            .mostWounded();
        if (target != null) {
            return target;
        }

        target = possibleTargets
            .ofType(
                AUnitType.Protoss_Reaver,
                AUnitType.Protoss_High_Templar,

                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Terran_Siege_Tank_Tank_Mode,

                AUnitType.Zerg_Defiler,
                AUnitType.Zerg_Guardian
            )
//                .inShootRangeOf(unit)
            .inRadius(15, unit)
            .mostWounded();
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target DT + Mutalisks

        target = possibleTargets
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

        target = possibleTargets
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

        target = possibleTargets
            .air()
            .inShootRangeOf(unit)
            .mostWounded();
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target TRANSPORT

//        target = possibleTargets
//                .transports(true)
//                .inRadius(10, unit)
//                .nearestTo(unit);
//        if (target != null) {
//            return target;
//        }

        // =========================================================
        // Target WORKERS

        target = possibleTargets
            .workers()
            .inShootRangeOf(unit)
            .mostWounded();
        if (target != null) {
            return target;
        }

        return null;
    }

    protected AUnit targetsStandard() {
        AUnit target;

        // =========================================================
        // Target WORKERS

        target = possibleTargets
            .workers()
            .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target DISTANT BASES, hoping to find workers

        target = possibleTargets
            .bases()
            .nearestTo(unit);
        ATargeting.debug("target AA1 = " + target + " // " + unit);

        if (target != null) {
            return target;
        }

        // =========================================================
        // Target DEFENSIVE BUILDINGS

        target = possibleTargets
            .ofType(AUnitType.Zerg_Sunken_Colony)
            .nearestTo(unit);
        ATargeting.debug("target AA2 = " + target + " // " + unit);

        if (target != null) {
            return target;
        }

        // =========================================================
        // Target COMBAT UNITS THAT CAN'T SHOOT AT US

        target = possibleTargets
            .combatUnits()
            .notHavingAntiAirWeapon()
            .nearestTo(unit);
        ATargeting.debug("target AA3 = " + target + " // " + unit);

        if (target != null) {
            return target;
        }

        // =========================================================
        // Target ANY COMBAT UNITS

//        target = possibleTargets
//                .notHavingAntiAirWeapon()
//                .inRadius(50, unit)
//                .nearestTo(unit);
//        ATargeting.debug("target AA4 = " + target + " // " + unit);
//
//        if (target != null) {
//            return target;
//        }

        // =========================================================
        // Target ANY COMBAT UNITS

        if (!onlyEnemiesInRangeAreAllowed) {
            target = possibleTargets
                .combatUnits()
                .nearestTo(unit);
            ATargeting.debug("target AA5 = " + target + " // " + unit);
        }

        if (target != null) {
            return target;
        }

        return null;
    }
}
