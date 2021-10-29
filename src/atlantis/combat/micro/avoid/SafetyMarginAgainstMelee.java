package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class SafetyMarginAgainstMelee extends SafetyMargin {

    public static double calculate(AUnit attacker, AUnit defender) {
        double criticalDist;

        if (defender.isInfantry()) {
            criticalDist = 1.3
                    + woundedBonus(defender);
        }
        else {
            criticalDist = enemyWeaponRangeBonus(defender, attacker)
                    + woundedBonus(defender)
                    + beastBonus(defender)
                    + ourUnitsNearbyBonus(defender)
                    + workerBonus(defender, attacker);

            if (!defender.isInfantry()) {
                criticalDist +=
                        ourMovementBonus(defender)
                                + quicknessBonus(defender, attacker)
                                + enemyMovementBonus(defender, attacker);
            }
        }

        criticalDist = Math.min(criticalDist, 3.7);
//        System.out.println("criticalDist = " + criticalDist + " // " + ourUnitsNearbyBonus(defender));

        return attacker.distTo(defender) - criticalDist;
    }

    // =========================================================

    protected static double beastBonus(AUnit defender) {
        int beastNearby = Select.enemy()
                .ofType(
                        AUnitType.Protoss_Archon,
                        AUnitType.Protoss_Dark_Templar,
                        AUnitType.Zerg_Ultralisk
                )
                .inRadius(5, defender)
                .count();

        return beastNearby > 0 ? 1.6 : 0;
    }

}
