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
        // Target COMBAT UNITS IN RANGE

        target = enemyUnits.clone()
                .combatUnits()
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            if (AEnemyTargeting.DEBUG) System.out.println("C1 = " + target);
            return target;
        }

        // =========================================================
        // Defensive buildings IN RANGE

        target = enemyBuildings.clone()
                .ofType(
                        AUnitType.Protoss_Photon_Cannon,
                        AUnitType.Zerg_Sunken_Colony
                )
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            if (AEnemyTargeting.DEBUG) System.out.println("C2 = " + target);
            return target;
        }

        return null;
    }

    private static AUnit targetOutsideShootingRange(AUnit unit) {
        AUnit target;

        // =========================================================
        // Special case - SHUTTLE

        if ((target = ATransportTargeting.target(unit)) != null) {
            if (AEnemyTargeting.DEBUG) System.out.println("C3 = " + target);
            return target;
        }

        // =========================================================
        // Target COMBAT UNITS IN RANGE

        target = enemyUnits.clone()
                .combatUnits()
                .inRadius(13, unit)
                .nearestTo(unit);
        if (target != null) {
            if (AEnemyTargeting.DEBUG) System.out.println("C4 = " + target);
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
                .effVisible()
                .canBeAttackedBy(unit, false, true)
                .nearestTo(unit);

//        if (AEnemyTargeting.DEBUG) System.out.println("C5 = " + target);
        return target;
    }

}
