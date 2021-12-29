package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class SafetyMarginAgainstMelee extends SafetyMargin {

    public static double ENEMIES_NEARBY_FACTOR = 0.3;
    public static double ENEMIES_NEARBY_MAX_DIST = 1.44;
//    public static double INFANTRY_BASE_IF_MEDIC = 1.60;
    public static double INFANTRY_BASE_IF_MEDIC = 1.80;
    public static int INFANTRY_WOUND_MODIFIER_WITH_MEDIC = 19;
    public static double INFANTRY_BASE_IF_NO_MEDIC = 2.65;
    public static int INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC = 40;
    private static final double INFANTRY_CRITICAL_HEALTH_BONUS_IF_MEDIC = 1.95;
    private static final double INFANTRY_CRITICAL_HEALTH_BONUS_IF_NO_MEDIC = 3.0;
//    public static double INFANTRY_BASE_IF_MEDIC = 0.64;
//    public static int INFANTRY_WOUND_IF_MEDIC = 20;
//    public static double INFANTRY_BASE_IF_NO_MEDIC = 2.02;
//    public static int INFANTRY_WOUND_IF_NO_MEDIC = 85;

    public static double calculate(AUnit defender, AUnit attacker) {
        double criticalDist;

        // Terran INFANTRY
        if (defender.isTerranInfantry()) {
            if (defender.hasMedicInRange()) {
                criticalDist = INFANTRY_BASE_IF_MEDIC
                        + enemyMeleeUnitsNearbyBonus(defender)
                        + woundedAgainstMeleeBonus(defender, attacker);
//                        + enemyMovementBonus(defender, attacker) / 2;

                criticalDist = Math.min(criticalDist, 3.0);
            }

            else {
                criticalDist = INFANTRY_BASE_IF_NO_MEDIC
                        + enemyMeleeUnitsNearbyBonus(defender)
                        + woundedAgainstMeleeBonus(defender, attacker);
//                        + ourMovementBonus(defender) / 4
//                        + enemyMovementBonus(defender, attacker);

//                System.out.println("criticalDist = " + criticalDist + " (hp = " + defender.hp() + ")");
                criticalDist += enemyUnitsNearbyBonus(defender) * ENEMIES_NEARBY_FACTOR;

                criticalDist = Math.min(criticalDist, 3.7);
            }
        }

        // VULTURE
        else if (defender.isVulture()) {
            criticalDist = 2.8
                    + woundedAgainstMeleeBonus(defender, attacker)
                    + ourMovementBonus(defender)
                    + enemyMovementBonus(defender, attacker);
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

            // 3.9 tiles (almost base width) should be enough as a minimum versus melee unit
            criticalDist = Math.min(criticalDist, 3.9);
        }

        if (defender.isRanged() && attacker.isWorker()) {
            criticalDist = 2.5;
        }

        return attacker.distTo(defender) - criticalDist;
    }

    // =========================================================

    private static double enemyMeleeUnitsNearbyBonus(AUnit defender) {
        if (defender.enemiesNearby().melee().inRadius(2.7, defender).atLeast(2)) {
            return 1.8;
        }

        return 0;
    }

    private static double baseForMelee(AUnit defender, AUnit attacker) {
        return attacker.isZealot() ? 0.5 : 0.7;
    }

    private static double enemyUnitsNearbyBonus(AUnit defender) {
        return Select.enemyCombatUnits().inRadius(ENEMIES_NEARBY_MAX_DIST, defender).count();
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

    protected static double woundedAgainstMeleeBonus(AUnit defender, AUnit attacker) {
        if (attacker.isRanged()) {
            return 0;
        }

        if (defender.isTerranInfantry()) {
            if (defender.hasMedicInRange()) {
                if (defender.hp() <= 18) {
                    return INFANTRY_CRITICAL_HEALTH_BONUS_IF_MEDIC;
                }
                return defender.woundPercent() / INFANTRY_WOUND_MODIFIER_WITH_MEDIC;
            } else {
                if (defender.hp() <= 22) {
                    return INFANTRY_CRITICAL_HEALTH_BONUS_IF_NO_MEDIC;
                }
                return defender.woundPercent() / INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC;
            }
        }

        else if (defender.isAir()) {
            return defender.woundPercent() / 10;
        }

        boolean applyExtraModifier = defender.isTank() || defender.isVulture();

        return (defender.woundPercent() * (applyExtraModifier ? 2 : 1)) / 32.0;
    }

}
