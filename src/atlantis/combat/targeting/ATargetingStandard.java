package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ATargetingStandard extends AEnemyTargeting {

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
        // OVERLORDS IN RANGE

        target = enemyUnits.clone()
                .ofType(AUnitType.Zerg_Overlord)
                .inShootRangeOf(2, unit)
                .nearestTo(unit);
        if (target != null) {
            System.out.println("OVERLORD target = " + target + " // ");
            System.out.println("dist = " + target.distTo(unit));
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
        // Target real units

        target = enemyUnits.clone()
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            return target;
        }

        // =========================================================
        // A bit further WORKERS

        target = enemyUnits.clone()
                .workers()
                .inRadius(10, unit)
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
        // Okay, try targeting any-fuckin-thing

        return Select.enemy()
                .effVisible()
                .canBeAttackedBy(unit, 5)
                .nearestTo(unit);
    }

}
