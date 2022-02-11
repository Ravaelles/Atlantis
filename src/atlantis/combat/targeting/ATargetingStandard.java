package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ATargetingStandard extends ATargeting {

    public static AUnit target(AUnit unit) {

        // =========================================================
        // =========================================================
        // =========== REMEMBER, AT THIS POINT =====================
        // ======== ENEMY IS AT MOST 15 TILES AWAY =================
        // =========================================================
        // =========================================================

        AUnit target;

        // =========================================================
        // WORKERS IN RANGE

        target = enemyUnits.clone()
                .workers()
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Quite near WORKERS

        target = enemyUnits.clone()
                .workers()
                .inRadius(8, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Target real units - exclude MEDICS

        target = enemyUnits.clone()
                .inShootRangeOf(unit)
                .excludeMedics()
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // A bit further WORKERS

        target = enemyUnits.clone()
                .workers()
                .inRadius(15, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Bases

        target = enemyBuildings.clone()
                .bases()
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Buildings worth destroying first

        target = enemyBuildings.clone()
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
                .inRadius(11, unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // Okay, try targeting any-fuckin-thing

        return Select.enemyRealUnits()
                .effVisible()
                .canBeAttackedBy(unit, 150)
                .nearestTo(unit);
    }

}
