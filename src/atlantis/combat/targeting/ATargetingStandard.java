package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class ATargetingStandard extends AEnemyTargeting {

    public static AUnit target(AUnit unit) {

        // =========================================================
        // =========================================================
        // =========== REMEMBER, AT THIS POINT =====================
        // ======== ENEMY IS AT MOST 15 TILES AWAY =================
        // =========================================================
        // =========================================================

        AUnit target;
        double groundRange = unit.groundWeaponRange();

        // =========================================================
        // Target real units

        target = units.clone()
                .inRadius(12, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Workers IN SHOT RANGE

        target = units.
                workers()
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Defensive buildings

        target = Select
                .enemy()
                .ofType(
                        AUnitType.Terran_Bunker
                )
                .nearestTo(unit);
        if (target != null) {

            // Target repairers
            AUnit repairer = Select.enemy().workers().inRadius(2, target).nearestTo(unit);
            if (repairer != null) {
                return repairer;
            }

            return target;
        }

        // =========================================================
        // Workers

        target = units.
                workers()
                .inRadius(8, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Buildings worth destroying first

        target = buildings.clone()
                .ofType(
                        AUnitType.Protoss_Fleet_Beacon,
                        AUnitType.Protoss_Cybernetics_Core,
                        AUnitType.Protoss_Templar_Archives,
                        AUnitType.Terran_Armory,
                        AUnitType.Terran_Engineering_Bay,
                        AUnitType.Terran_Academy,
                        AUnitType.Zerg_Spawning_Pool,
                        AUnitType.Zerg_Spire,
                        AUnitType.Zerg_Greater_Spire
                )
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Bases

        target = buildings.clone()
                .bases()
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Okay, try targeting any-fuckin-thing

        return Select.enemy().effVisible().nearestTo(unit);
    }

}
