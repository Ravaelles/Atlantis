package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class ATargetingImportant extends AEnemyTargeting {

    public static AUnit target(AUnit unit) {

        // =========================================================
        // =========================================================
        // =========== REMEMBER, AT THIS POINT =====================
        // ======== ENEMY IS AT MOST 15 TILES AWAY =================
        // =========================================================
        // =========================================================

        AUnit target = null;
        double groundRange = unit.getWeaponRangeGround();

        // =========================================================
        // Target COMBAT UNITS IN RANGE

        target = units.clone()
                .combatUnits()
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Defensive buildings

        target = buildings.clone()
                .ofType(
                        AUnitType.Protoss_Photon_Cannon,
                        AUnitType.Zerg_Sunken_Colony
                )
                .inRadius(12, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Including unfinished defensive buildings

        target = Select.enemy()
                .canBeAttackedBy(unit, false, true)
                .ofType(
                        AUnitType.Protoss_Photon_Cannon,
                        AUnitType.Zerg_Sunken_Colony,
                        AUnitType.Zerg_Creep_Colony,
                        AUnitType.Zerg_Spore_Colony,
                        AUnitType.Terran_Bunker,
                        AUnitType.Terran_Missile_Turret
                )
                .inRadius(12, unit)
                .nearestTo(unit);
        return target;

        // =========================================================
    }

}
