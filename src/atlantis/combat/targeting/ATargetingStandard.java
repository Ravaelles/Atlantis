package atlantis.combat.targeting;

import atlantis.game.A;
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

        target = enemyUnits
                .workers()
                .inShootRangeOf(unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("D1 = " + target);
            return target;
        }

        // =========================================================
        // Quite near WORKERS

        target = enemyUnits
                .workers()
                .inRadius(unit.isMelee() ? 6 : 10, unit)
                .nearestTo(unit);

//        if (unit.isDragoon()) {
//            System.err.println("### workers = "
//                + "A = " + enemyUnits.count()
//                + " // B = " + enemyUnits.workers().count()
//                + " // C = " + enemyUnits.workers().inRadius(unit.isMelee() ? 6 : 10, unit).count()
//                + " // D = " + enemyUnits.workers().inRadius(unit.isMelee() ? 6 : 10, unit).nearestTo(unit)
////                + "\n // E = " + enemyUnits.workers().nearestTo(unit)
////                + " // F = " + unit
////                + " // G = " + A.dist(unit, enemyUnits.workers().nearestTo(unit))
//            );
//        }
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("D2 = " + target);
            return target;
        }

        // =========================================================
        // Target real units - exclude MEDICS

        target = enemyUnits
                .excludeMedics()
                .nearestTo(unit);

        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("D3 = " + target);
            return target;
        }

        // =========================================================
        // Bases

        target = enemyBuildings
                .bases()
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("D4 = " + target);
            return target;
        }

        // =========================================================
        // A bit further WORKERS

        target = enemyUnits
                .workers()
                .inRadius(17, unit)
                .nearestTo(unit);
        if (target != null && Select.enemies(target.type()).inRadius(3, unit).atLeast(3)) {
            if (ATargeting.DEBUG) System.out.println("D5 = " + target);
            return target;
        }

        // =========================================================
        // Buildings worth destroying first

        target = enemyBuildings
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
                .inRadius(25, unit)
                .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("D6 = " + target);
            return target;
        }

        // =========================================================
        // Okay, try targeting any-fuckin-thing

        // Non medics nearby
        target = unit.enemiesNear()
            .nonBuildings()
            .excludeMedics()
            .canBeAttackedBy(unit, 15)
            .nearestTo(unit);
        if (target != null) {
            if (ATargeting.DEBUG) System.out.println("D7 = " + target);
            return target;
        }

        // =========================================================

        target = unit.enemiesNear()
            .havingPosition()
            .canBeAttackedBy(unit, 150)
            .nearestTo(unit);

        if (target != null && ATargeting.DEBUG) System.out.println("D8 = " + target);
        return target;
    }

}
