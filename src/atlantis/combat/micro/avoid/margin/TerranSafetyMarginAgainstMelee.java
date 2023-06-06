package atlantis.combat.micro.avoid.margin;

import atlantis.game.A;
import atlantis.units.AUnit;

public class TerranSafetyMarginAgainstMelee {
    protected static double handleTerranInfantry(AUnit defender, AUnit attacker) {
        if (canIgnoreThisEnemyForNow(defender, attacker)) {
            return 2;
        }

        // =========================================================

        double criticalDist;

//        if (true) return 3;

        // === MEDIC in range ===========================================

        boolean medicInRange = defender.hasMedicInRange();
        if (medicInRange) {
            if (defender.isHealthy() && defender.enemiesNearInRadius(3) <= 1) {
                defender.setTooltipTactical("Healthy");
                return SafetyMarginAgainstMelee.enemyUnitsNearBonus(defender);
            }

            criticalDist = SafetyMarginAgainstMelee.INFANTRY_BASE_IF_MEDIC
//                    + ourMovementBonus(defender)
//                    + enemyMovementBonus(defender, attacker)
                    + SafetyMarginAgainstMelee.woundedAgainstMeleeBonus(defender, attacker);

            defender.setTooltipTactical("HasMedic");
        }

        // === No medic Near ===========================================

        else {
            criticalDist = SafetyMarginAgainstMelee.INFANTRY_BASE_IF_NO_MEDIC
//                    + ourMovementBonus(defender)
                    + (1.05 * SafetyMargin.enemyMovementBonus(defender, attacker))
//                    + (defender.hasCooldown() ? enemyMovementBonus(defender, attacker) : 0)
                    + SafetyMargin.workerBonus(defender, attacker)
                    + Math.min(1.2, SafetyMarginAgainstMelee.woundedAgainstMeleeBonus(defender, attacker));

//            criticalDist = Math.min(criticalDist, defender.isWounded() ? 2.9 : 2.8);

            String log = "NoMedic" + A.digit(criticalDist);
            defender.setTooltipTactical(log);
//            defender.addLog(log);
        }

        criticalDist += SafetyMarginAgainstMelee.enemyUnitsNearBonus(defender);

        if (medicInRange) {
            criticalDist = Math.min(criticalDist, 2.1);
        }
//        else {
//            criticalDist = Math.min(criticalDist, 3.0);
//        }

//        defender.addTooltip(A.digit(criticalDist));

        criticalDist = Math.min(3.7, criticalDist);

        return criticalDist;
    }

    private static boolean canIgnoreThisEnemyForNow(AUnit defender, AUnit attacker) {
        if (attacker.isRanged()) {
            return false;
        }

        double distTo = defender.distTo(attacker);
        if (distTo >= 4) {
            return true;
        }

        if (distTo >= 2 && !defender.isOtherUnitFacingThisUnit(attacker)) {
            return true;
        }

        return false;
    }
}
