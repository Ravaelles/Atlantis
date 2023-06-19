package atlantis.combat.micro.avoid.margin;

import atlantis.combat.retreating.RetreatManager;
import atlantis.debug.painter.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Color;

public class SafetyMarginAgainstRanged extends SafetyMargin {

//    private static final double MIN_DIST_TO_COMBAT_BUILDING = 8.7;

    public static double calculate(AUnit defender, AUnit attacker) {
        double criticalDist;

        // GROUND unit
        if (defender.isGroundUnit()) {
            criticalDist = forGroundUnit(defender, attacker);

            if (defender.isGhost()) {
                criticalDist += bonusForGhost(defender, attacker);
            }
            else if (defender.isWraith()) {
                criticalDist += bonusForWraith(defender, attacker);
            }
        }

        // AIR unit
        else {
            criticalDist = forAirUnit(defender, attacker);
        }

        // === For all ==================================

        criticalDist += addBuildingBonus(defender, attacker, criticalDist);
        criticalDist += shouldRetreatBonus(defender);
//            addBuildingBonus(defender, attacker, criticalDist)
//        if (attacker.isCombatBuilding()) {
//            System.out.println(defender + ", CRIT_DIST = " + criticalDist + " // " + addBuildingBonus(defender, attacker, criticalDist));
//        }

        // ==============================================

        return criticalDist;
    }

    private static double bonusForWraith(AUnit defender, AUnit attacker) {
        if (attacker.isDragoon()) {
            return 2.3;
        }

        return 1.3;
    }

    private static double bonusForGhost(AUnit defender, AUnit attacker) {
        if (attacker.isCombatBuilding()) {
            return 7;
        }

        return defender.woundPercent() / 25.0;
    }

    private static double shouldRetreatBonus(AUnit defender) {
        if (RetreatManager.getCachedShouldRetreat(defender)) {
            return 4.2;
        }

        return 0;
    }

    private static double forGroundUnit(AUnit defender, AUnit attacker) {
        return enemyWeaponRange(defender, attacker)
                + quicknessBonus(defender, attacker)
                + lurkerBonus(defender, attacker)
                + woundedBonus(defender, attacker)
                + ourUnitsNearBonus(defender)
                + ourMovementBonus(defender)
                + enemyMovementBonus(defender, attacker)
                + scoutBonus(defender, attacker)
                + combatEvalBonus(defender, attacker)
                + workerBonus(defender, attacker);
    }

    private static double forAirUnit(AUnit defender, AUnit attacker) {
        return 3
                + enemyWeaponRange(defender, attacker)
                + woundedBonus(defender, attacker)
                + specialAirUnitBonus(defender)
                + ourMovementBonus(defender)
                + enemyMovementBonus(defender, attacker);
//        return applyAirUnitTweaks(defender, attacker);
    }

    // =========================================================

    protected static double woundedBonus(AUnit defender, AUnit attacker) {
        if (defender.isDragoon() && defender.hpLessThan(25)) {
            return 3.4;
        }

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

        APainter.paintTextCentered(attacker, "DefBuilding", Color.Orange);
        return 1.6 + defender.woundPercent() / 100.0 + (defender.isMoving() ? 0.5 : 0) + (defender.isAir() ? 0.5 : 0);
    }

//    private static double extraMarginAgainstCombatBuilding(AUnit defender, AUnit attacker) {
//        if (defender.isVulture()) {
//            return 6.4;
//        } else if (defender.is(AUnitType.Terran_Wraith)) {
//            return 7.3;
//        }
//
//        return (defender.isAir() ? 5.8 : 1.1);
//    }

    private static double lurkerBonus(AUnit defender, AUnit attacker) {
        if (attacker.is(AUnitType.Zerg_Lurker) && attacker.effUndetected()) {
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
