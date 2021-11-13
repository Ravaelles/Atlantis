package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class SafetyMarginAgainstMelee extends SafetyMargin {

    public static double calculate(AUnit attacker, AUnit defender) {

//        if (defender.isInfantry()) {
//            criticalDist = enemyWeaponRangeBonus(defender, attacker)
//                    + defender.woundPercent() / 19
//                    + ourMovementBonus(defender)
//                    + enemyMovementBonus(defender, attacker);
//        }
//        else {
        double criticalDist = baseForMelee(defender, attacker)
                + enemyWeaponRangeBonus(defender, attacker)
                + woundedAgainstMeleeBonus(defender)
                + beastBonus(defender)
                + ourUnitsNearbyBonus(defender)
                + workerBonus(defender, attacker)
                + ourMovementBonus(defender)
                + quicknessBonus(defender, attacker)
                + enemyMovementBonus(defender, attacker);
//        }

        criticalDist = Math.min(criticalDist, 3.85);
//        System.out.println("criticalDist = " + criticalDist + " // " + ourUnitsNearbyBonus(defender));

        return attacker.distTo(defender) - criticalDist;
    }

    // =========================================================

    private static double baseForMelee(AUnit defender, AUnit attacker) {
//        return 0.1;
        return attacker.isZealot() ? 0.5 : 0.7;
    }

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

    protected static double woundedAgainstMeleeBonus(AUnit defender) {
        if (defender.isAirUnit()) {
            return defender.woundPercent() / 10;
        }

        boolean applyExtraModifier = defender.isTank();
        return (defender.woundPercent() * (applyExtraModifier ? 2 : 1)) / 32.0;
    }

}
