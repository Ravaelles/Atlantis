package atlantis.combat.micro.avoid;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Color;

public class SafetyMarginAgainstRanged extends SafetyMargin {

    private static final double MIN_DIST_TO_COMBAT_BUILDING = 8.2;

    public static double calculate(AUnit defender, AUnit attacker) {
        double criticalDist;

        // GROUND unit
        if (defender.isGroundUnit()) {
            criticalDist = forGroundUnit(defender, attacker);
        }

        // AIR unit
        else {
            criticalDist = forAirUnit(defender, attacker);
        }

        // === For all ==================================

        criticalDist += addBuildingBonus(defender, attacker, criticalDist);
//        if (attacker.isCombatBuilding()) {
//            System.out.println(defender + ", CRIT_DIST = " + criticalDist + " // " + addBuildingBonus(defender, attacker, criticalDist));
//        }

        // ==============================================

        return attacker.distTo(defender) - criticalDist;
    }

    private static double forGroundUnit(AUnit defender, AUnit attacker) {
        return enemyWeaponRange(defender, attacker)
                + quicknessBonus(defender, attacker)
                + lurkerBonus(defender, attacker)
                + woundedBonus(defender, attacker)
                + ourUnitsNearbyBonus(defender)
                + ourMovementBonus(defender)
                + enemyMovementBonus(defender, attacker)
                + scoutBonus(defender, attacker)
                + combatEvalBonus(defender, attacker);
    }

    private static double forAirUnit(AUnit defender, AUnit attacker) {
        return 3
                + enemyWeaponRange(defender, attacker)
                + woundedBonus(defender, attacker)
                + transportBonus(defender)
                + ourMovementBonus(defender)
                + enemyMovementBonus(defender, attacker);
//        return applyAirUnitTweaks(defender, attacker);
    }

    // =========================================================

    protected static double woundedBonus(AUnit defender, AUnit attacker) {

        // Don't apply wound bonus against units with bigger or equal range
        if (attacker.groundWeaponRange() >= defender.groundWeaponRange()) {
            return 0;
        }

        return SafetyMargin.woundedBonus(defender, attacker);
    }

    private static double addBuildingBonus(AUnit defender, AUnit attacker, double criticalDist) {
        if (!attacker.isCombatBuilding()) {
            return 0;
        }

        if (attacker.isCombatBuilding()) {
            APainter.paintTextCentered(attacker, "DefBuilding", Color.Orange);
        }

        criticalDist += extraMarginAgainstCombatBuilding(defender, attacker);

        if (criticalDist <= MIN_DIST_TO_COMBAT_BUILDING) {
            criticalDist = MIN_DIST_TO_COMBAT_BUILDING;
        }

        return criticalDist;
    }

    private static double extraMarginAgainstCombatBuilding(AUnit defender, AUnit attacker) {
        if (defender.isVulture()) {
            return 6.4;
        } else if (defender.is(AUnitType.Terran_Wraith)) {
            return 7.1;
        }

        return (defender.isAir() ? 5.8 : 3.5);
    }

    private static double lurkerBonus(AUnit defender, AUnit attacker) {
        if (attacker.is(AUnitType.Zerg_Lurker) && attacker.effCloaked()) {
            return 3.6;
        }

        return 0;
    }

    private static double scoutBonus(AUnit defender, AUnit attacker) {
        return defender.isScout() ? (5 + defender.woundPercent() / 33) : 0;
    }

    private static double combatEvalBonus(AUnit defender, AUnit attacker) {
//        if (!ACombatEvaluator.isSituationFavorable(defender)) {
//            return -3;
//        }

        return 0;
    }

}
