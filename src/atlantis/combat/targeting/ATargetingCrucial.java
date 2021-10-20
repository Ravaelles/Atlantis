package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ATargetingCrucial extends AEnemyTargeting {

    public static AUnit target(AUnit unit) {

        // =========================================================
        // =========================================================
        // =========== REMEMBER, AT THIS POINT =====================
        // ======== ENEMY IS AT MOST 15 TILES AWAY =================
        // =========================================================
        // =========================================================

        AUnit target = null;
        double groundRange = unit.getWeaponRangeGround();
        double airRange = unit.getWeaponRangeAir();

        // =========================================================
        // Attack MINES

        target = units.clone()
                .ofType(AUnitType.Terran_Vulture_Spider_Mine)
                .inShootRangeOf(groundRange + 4, unit)
                .randomWithSeed(unit.getID());
        if (target != null) {
            return target;
        }

        // =========================================================
        // Targetable observers

        target = units.clone()
                .ofType(AUnitType.Protoss_Observer)
                .effVisible()
                .inRadius(unit.isAirUnit() ? 12 : 9, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // DEADLIEST shit out there,
        // Move to attack it WAY NOT IN RANGE

        target = units.clone()
                .ofType(
                        AUnitType.Zerg_Defiler,
                        AUnitType.Protoss_Carrier,
                        AUnitType.Terran_Siege_Tank_Tank_Mode,
                        AUnitType.Terran_Siege_Tank_Siege_Mode
                )
                .inShootRangeOf(unit)
//                .inRadius(10, unit)
                .mostWounded();
        if (target != null) {
            return target;
        }

        target = units.clone()
                .ofType(
                        AUnitType.Zerg_Defiler,
                        AUnitType.Protoss_Carrier,
                        AUnitType.Terran_Siege_Tank_Tank_Mode,
                        AUnitType.Terran_Siege_Tank_Siege_Mode
                )
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // DEADLY, but ALLOW SMALL OUT OF RANGE bonus

        target = units.clone()
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

        target = units.clone()
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

        target = units.clone()
                .ofType(
                        AUnitType.Protoss_Archon,
                        AUnitType.Protoss_Observer,
                        AUnitType.Zerg_Lurker,
                        AUnitType.Zerg_Ultralisk
                ).nearestTo(unit);
        return target;

        // =========================================================
    }

}
