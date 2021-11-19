package atlantis.combat.micro.avoid;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Color;

public class SafetyMarginAgainstRanged extends SafetyMargin {

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

        return attacker.distTo(defender) - criticalDist;
    }

    private static double forGroundUnit(AUnit defender, AUnit attacker) {
        return enemyWeaponRange(defender, attacker)
                + quicknessBonus(defender, attacker)
                + lurkerBonus(defender, attacker)
                + buildingBonus(defender, attacker)
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

    private static double buildingBonus(AUnit defender, AUnit attacker) {
        APainter.paintCircle(attacker, 5, Color.Red);
        APainter.paintCircle(attacker, 7, Color.Red);
        APainter.paintCircle(attacker, 9, Color.Red);
        APainter.paintCircle(attacker, 11, Color.Red);
        return attacker.isCombatBuilding() ? 3 : 0;
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
