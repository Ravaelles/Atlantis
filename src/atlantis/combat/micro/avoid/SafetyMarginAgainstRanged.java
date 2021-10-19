package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.util.A;

public class SafetyMarginAgainstRanged extends SafetyMargin {

    public static double calculate(AUnit attacker, AUnit defender) {
        double criticalDist = enemyWeaponRangeBonus(defender, attacker)
                + quicknessBonus(defender, attacker)
                + woundedBonus(defender)
                + ourUnitsNearbyBonus(defender)
                + ourMovementBonus(defender)
                + enemyMovementBonus(defender, attacker);

        return A.inRange(0.0, criticalDist, 12.5);
    }

}
