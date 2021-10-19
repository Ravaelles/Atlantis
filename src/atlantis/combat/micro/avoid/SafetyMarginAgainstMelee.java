package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.A;

public class SafetyMarginAgainstMelee extends SafetyMargin {

    public static double calculate(AUnit attacker, AUnit defender) {
        double criticalDist = enemyWeaponRangeBonus(defender, attacker)
                        + quicknessBonus(defender, attacker)
                        + woundedBonus(defender)
                        + beastBonus(defender)
                        + ourUnitsNearbyBonus(defender)
                        + ourMovementBonus(defender)
                        + enemyMovementBonus(defender, attacker);

        return A.inRange(0.0, criticalDist, 3.9);
    }

    // =========================================================

    protected static double beastBonus(AUnit defender) {
        int beastNearby = Select.enemy()
                .ofType(AUnitType.Protoss_Archon, AUnitType.Zerg_Ultralisk)
                .inRadius(5, defender)
                .count();

        return beastNearby > 0 ? 1.2 : 0;
    }

}