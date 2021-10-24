package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;

public class SafetyMarginAgainstRanged extends SafetyMargin {

    public static double calculate(AUnit attacker, AUnit defender) {
        double criticalDist = enemyWeaponRangeBonus(defender, attacker)
                + quicknessBonus(defender, attacker)
                + woundedBonus(defender)
                + transportBonus(defender)
                + ourUnitsNearbyBonus(defender)
                + ourMovementBonus(defender)
                + enemyMovementBonus(defender, attacker);

        return attacker.distTo(defender) - criticalDist;
    }

}
