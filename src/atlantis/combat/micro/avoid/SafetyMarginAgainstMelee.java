package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class SafetyMarginAgainstMelee extends SafetyMargin {

    public static double INFANTRY_BASE = 0.64;
    public static int INFANTRY_WOUND = 20;

    public static double calculate(AUnit defender, AUnit attacker) {

        double criticalDist;

        // Terran INFANTRY
        if (defender.isTerranInfantry()) {
            criticalDist = INFANTRY_BASE
                    + woundedAgainstMeleeBonus(defender, attacker);
//            +ourMovementBonus(defender) / 3
//                    + enemyMovementBonus(defender, attacker) / 3;

//            criticalDist += enemyUnitsNearbyBonus(defender, criticalDist) * ENEMIES_NEARBY_FACTOR;
        }

        // VULTURE
        else if (defender.isVulture()) {
            criticalDist = 3.6;
        }

        // Standard unit
        else {
            criticalDist = baseForMelee(defender, attacker)
                    + enemyWeaponRange(defender, attacker)
                    + woundedAgainstMeleeBonus(defender, attacker)
                    + beastBonus(defender)
                    + ourUnitsNearbyBonus(defender)
                    + workerBonus(defender, attacker)
                    + ourMovementBonus(defender)
                    + quicknessBonus(defender, attacker)
                    + enemyMovementBonus(defender, attacker);
        }

        // 3.9 tiles (almost base width) should be enough as a minimum versus melee unit
        criticalDist = Math.min(criticalDist, 3.9);

        return attacker.distTo(defender) - criticalDist;
    }

    // =========================================================

    private static double baseForMelee(AUnit defender, AUnit attacker) {
        return attacker.isZealot() ? 0.5 : 0.7;
    }

//    private static double enemyUnitsNearbyBonus(AUnit defender, double radius) {
//        return Select.enemyCombatUnits().inRadius(radius, defender).count();
//    }

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

    protected static double woundedAgainstMeleeBonus(AUnit defender, AUnit attacker) {
        if (defender.isTerranInfantry()) {
            return defender.woundPercent() / INFANTRY_WOUND;
        }

        if (defender.isAirUnit()) {
            return defender.woundPercent() / 10;
        }

        boolean applyExtraModifier = defender.isTank();
        return (defender.woundPercent() * (applyExtraModifier ? 2 : 1)) / 32.0;
    }

}
