package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class SafetyMarginAgainstRanged extends SafetyMargin {

    public static double calculate(AUnit attacker, AUnit defender) {
        double criticalDist;

        // GROUND unit
        if (defender.isGroundUnit()) {
            criticalDist = forGroundUnit(attacker, defender);
        }

        // AIR unit
        else {
            criticalDist = forAirUnit(attacker, defender);
        }

        return attacker.distTo(defender) - criticalDist;
    }

    private static double forGroundUnit(AUnit attacker, AUnit defender) {
        return enemyWeaponRange(defender, attacker)
                + quicknessBonus(defender, attacker)
                + lurkerBonus(defender, attacker)
                + buildingBonus(defender, attacker)
                + woundedBonus(defender)
                + ourUnitsNearbyBonus(defender)
                + ourMovementBonus(defender)
                + enemyMovementBonus(defender, attacker)
                + scoutBonus(defender, attacker)
                + combatEvalBonus(defender, attacker);
    }

    private static double forAirUnit(AUnit attacker, AUnit defender) {
        return 3
                + enemyWeaponRange(defender, attacker)
                + woundedBonus(defender)
                + transportBonus(defender)
                + ourMovementBonus(defender)
                + enemyMovementBonus(defender, attacker);
//        return applyAirUnitTweaks(defender, attacker);
    }

    // =========================================================

//    private static double applyAirUnitTweaks(AUnit defender, AUnit attacker) {
//        double attackerRangeWithMargin = attacker.airWeaponRange() + 3.8;
//
//        if (defender.isMutalisk() && defender.hp() >= 50) {
//            currentCriticalDist -= 2;
//        }
//
//        if (currentCriticalDist <= attackerRangeWithMargin) {
//            return attackerRangeWithMargin;
//        }
//
//        return currentCriticalDist;
//    }

    private static double buildingBonus(AUnit defender, AUnit attacker) {
        return attacker.type().isCombatBuilding() ? 3 : 0;
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
