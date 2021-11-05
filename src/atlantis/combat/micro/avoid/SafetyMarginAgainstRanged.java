package atlantis.combat.micro.avoid;

import atlantis.combat.eval.ACombatEvaluator;
import atlantis.units.AUnit;

public class SafetyMarginAgainstRanged extends SafetyMargin {

    public static double calculate(AUnit attacker, AUnit defender) {
        double criticalDist = enemyWeaponRangeBonus(defender, attacker)
                + quicknessBonus(defender, attacker)
                + woundedBonus(defender)
                + transportBonus(defender)
                + ourUnitsNearbyBonus(defender)
                + ourMovementBonus(defender)
                + enemyMovementBonus(defender, attacker)
                + combatEvalBonus(defender, attacker);

        return attacker.distTo(defender) - criticalDist;
    }

    // =========================================================

    private static double combatEvalBonus(AUnit defender, AUnit attacker) {
        if (!ACombatEvaluator.isSituationFavorable(defender)) {
            return -3;
        }

        return 0;
    }

}
