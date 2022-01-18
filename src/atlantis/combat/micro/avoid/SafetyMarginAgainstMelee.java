package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class SafetyMarginAgainstMelee extends SafetyMargin {

    public static double ENEMIES_NEARBY_FACTOR = 0.3;
    public static double ENEMIES_NEARBY_MAX_DIST = 1.44;
    public static double INFANTRY_BASE_IF_MEDIC = 0;
    public static int INFANTRY_WOUND_MODIFIER_WITH_MEDIC = 19;
    public static double INFANTRY_BASE_IF_NO_MEDIC = 2.65;
    public static int INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC = 33;
    private static final double INFANTRY_CRITICAL_HEALTH_BONUS_IF_MEDIC = 1.95;
    private static final double INFANTRY_CRITICAL_HEALTH_BONUS_IF_NO_MEDIC = 3.0;

    public static double calculate(AUnit defender, AUnit attacker) {
        double criticalDist = -1;

        // === Protoss ===============================================

        if (defender.isProtoss()) {
            criticalDist = handleProtoss(defender, attacker);
        }
        // === Terran ===============================================

        else if (defender.isTerran()) {
            criticalDist = handleTerran(defender, attacker);
        }

        // === Standard unit =========================================

        if (criticalDist <= -1) {
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

        return criticalDist;
    }

    // =========================================================

    private static double handleProtoss(AUnit defender, AUnit attacker) {
        if (
                defender.isDragoon()
                        && !attacker.isDT()
//                && (
//                        defender.shieldDamageAtMost(6)
//                        || (defender.shields() >= 6 && defender.lastAttackFrameMoreThanAgo(80))
//                        || (defender.hp() >= 21 && defender.lastAttackFrameMoreThanAgo(30 * 6))
//                )
        ) {
            if (defender.shieldDamageAtMost(6)) {
                return 0;
            }
            else if (
                    defender.hasNotMovedInAWhile()
                            && defender.shieldDamageAtMost(30)
                            && defender.lastUnderAttackMoreThanAgo(60)
            ) {
                return 0;
            }
            else if (
                    defender.shieldDamageAtMost(30)
                    && (
                            defender.lastAttackFrameMoreThanAgo(80)
                            || defender.lastUnderAttackMoreThanAgo(90)
                    )
            ) {
                return 2
                        + woundedAgainstMeleeBonus(defender, attacker)
                        + ourMovementBonus(defender)
                        + quicknessBonus(defender, attacker)
                        + enemyMovementBonus(defender, attacker);
            }
            else if (defender.hp() >= 21 && defender.lastAttackFrameMoreThanAgo(30 * 8)) {
                return 2.6;
            }
        }

        return -1;
    }

    private static double handleTerran(AUnit defender, AUnit attacker) {

        // === Terran INFANTRY =======================================

        if (defender.isTerranInfantry()) {
            return handleTerranInfantry(defender, attacker);
        }

        // === VULTURE ===============================================

        else if (defender.isVulture()) {
            return Math.min(
                    3.6,
                    2.5 + woundedAgainstMeleeBonus(defender, attacker)
                        + ourMovementBonus(defender)
                        + enemyMovementBonus(defender, attacker)
            );
        }

        return -1;
    }

    // =========================================================

    private static double enemyMeleeUnitsNearbyBonus(AUnit defender) {
        if (defender.meleeEnemiesNearbyCount() >= 2) {
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

        else if (defender.isVulture()) {
            return defender.woundPercent() / 30;
        }

        boolean applyExtraModifier = defender.isTank() || defender.isVulture();

        return (defender.woundPercent() * (applyExtraModifier ? 2 : 1)) / 32.0;
    }

    // =========================================================

    private static double handleTerranInfantry(AUnit defender, AUnit attacker) {
        double criticalDist;

        if (defender.hasMedicInRange()) {
            criticalDist = INFANTRY_BASE_IF_MEDIC
                    + enemyMeleeUnitsNearbyBonus(defender)
                    + ourMovementBonus(defender)
                    + enemyMovementBonus(defender, attacker)
                    + woundedAgainstMeleeBonus(defender, attacker);

            criticalDist = Math.min(criticalDist, 2.5);
        }

        // No medic nearby
        else {
            criticalDist = INFANTRY_BASE_IF_NO_MEDIC
                    + enemyMeleeUnitsNearbyBonus(defender)
                    + ourMovementBonus(defender)
                    + enemyMovementBonus(defender, attacker)
                    + woundedAgainstMeleeBonus(defender, attacker);

            if (
                    defender.hp() >= 24
                            && defender.friendsNearbyCount() >= 5
                            && 4 * defender.friendsNearbyCount() >= defender.meleeEnemiesNearbyCount()
            ) {
                criticalDist = 1.7;
            }

//                System.out.println("criticalDist = " + criticalDist + " (hp = " + defender.hp() + ")");
            criticalDist += enemyUnitsNearbyBonus(defender) * ENEMIES_NEARBY_FACTOR;

            criticalDist = Math.min(criticalDist, defender.isWounded() ? 3.2 : 2.5);
        }

        return criticalDist;
    }

}
