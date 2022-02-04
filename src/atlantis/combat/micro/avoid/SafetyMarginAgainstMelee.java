package atlantis.combat.micro.avoid;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

import static atlantis.units.AUnitType.Protoss_Zealot;

public class SafetyMarginAgainstMelee extends SafetyMargin {

//    public static double ENEMIES_NEARBY_FACTOR = 0.3;
//    public static double ENEMIES_NEARBY_MAX_DIST = 1.44;
    public static double INFANTRY_BASE_IF_MEDIC = 0;
    public static int INFANTRY_WOUND_MODIFIER_WITH_MEDIC = 19;
    public static double INFANTRY_BASE_IF_NO_MEDIC = 2.65;
    public static int INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC = 33;
    private static final double INFANTRY_CRITICAL_HEALTH_BONUS_IF_MEDIC = 1.95;
    private static final double INFANTRY_CRITICAL_HEALTH_BONUS_IF_NO_MEDIC = 3.0;

    public static double calculate(AUnit defender, AUnit attacker) {
        double criticalDist;

        // === Protoss ===============================================

        if ((criticalDist = forDragoon(defender, attacker)) >= 0) {
            return criticalDist;
        }
        else if (defender.isDT()) {
            return 0;
        }

        // === Terran ===============================================

        if (defender.isTerran()) {
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

            // This should be enough as a minimum versus melee units
            criticalDist = Math.min(criticalDist, defender.isDragoon() ? 2.7 : 3.4);
        }

        if (defender.isRanged() && attacker.isWorker()) {
            criticalDist = 2.3;
        }

        return criticalDist;
    }

    private static double forDragoon(AUnit defender, AUnit attacker) {
        if (!defender.isDragoon()) {
            return -1;
        }

        double base = woundedAgainstMeleeBonus(defender, attacker);
        boolean enemyFacingUs = defender.isOtherUnitFacingThisUnit(attacker);

        if (attacker.isWorker() && attacker.hp() >= 30) {
            base += 0.5;
        }

        else if (!enemyFacingUs && defender.shieldDamageAtMost(40)) {
            base += 1.5;
        }

        else if (
                (attacker.hp() <= 16 || defender.shieldDamageAtMost(38))
                        && (
                        !enemyFacingUs
                                || defender.lastAttackFrameMoreThanAgo(130)
                                || (defender.lastAttackFrameMoreThanAgo(90) && attacker.hpPercent(30))
                                || (defender.lastAttackFrameMoreThanAgo(40) && defender.lastUnderAttackMoreThanAgo(150))
                )
        ) {
            defender.addLog("CoolDragoon_" + defender.lastAttackFrameAgo());
            base += (defender.isHealthy() ? 0 : 0.3);
            //            criticalDist = handleDragoon(defender, attacker);
        }

        // =========================================================

        base = Math.min(2.4, base);
        return base;

//        if (Missions.isGlobalMissionDefend()) {
//            return base;
//        }
//
//        return -1;
    }

    // =========================================================

//    private static double handleDragoon(AUnit defender, AUnit attacker) {
//        if (!defender.isDragoon() || attacker.isDT()) {
//            return -1;
//        }
//
//        double min = -0.5;
//        double safer = 1.2;
//
//        if (defender.shieldDamageAtMost(12)) {
//            return min;
//        } else if (
//                defender.hasNotMovedInAWhile()
//                        && defender.shieldDamageAtMost(30)
////                        && defender.lastUnderAttackMoreThanAgo(60)
//        ) {
//            return safer;
//        } else if (
//                defender.shieldDamageAtMost(40) &&
//                        (
//                                defender.lastAttackFrameMoreThanAgo(80)
////                                        || defender.lastUnderAttackMoreThanAgo(90)
//                        )
//        ) {
//            return safer
//                    + woundedAgainstMeleeBonus(defender, attacker)
//                    + ourMovementBonus(defender)
//                    + quicknessBonus(defender, attacker)
//                    + enemyMovementBonus(defender, attacker);
//        }
////        else if (defender.hp() >= 21 && defender.lastAttackFrameMoreThanAgo(30 * 3)) {
////            return safe;
////        }
//
//        return -1;
//    }

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
//        return Select.enemyCombatUnits().inRadius(ENEMIES_NEARBY_MAX_DIST, defender).count();

        if (defender.enemiesNearby().ofType(Protoss_Zealot).inRadius(2, defender).atLeast(3)) {
            return 2.3;
        }

        if (defender.enemiesNearby().ofType(Protoss_Zealot).inRadius(2, defender).atLeast(2)) {
            return 1.6;
        }

        return 0;
    }

    protected static double beastBonus(AUnit defender) {
        if (
                defender.enemiesNearby()
                .ofType(AUnitType.Protoss_Dark_Templar)
                .inRadius(5, defender)
                .notEmpty()
        ) {
            return 2.6;
        }

        int beastNearby = defender.enemiesNearby()
                .ofType(AUnitType.Protoss_Archon, AUnitType.Zerg_Ultralisk)
                .inRadius(5, defender)
                .count();

        return beastNearby > 0 ? 1.8 : 0;
    }

    protected static double woundedAgainstMeleeBonus(AUnit defender, AUnit attacker) {
//        if (attacker.isRanged()) {
//            return 2;
//        }

        if (defender.isZealot()) {
            return defender.hpLessThan(21) ? 1.8 : 0;
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
        } else if (defender.isAir()) {
            return defender.woundPercent() / 10;
        } else if (defender.isVulture()) {
            return defender.woundPercent() / 30;
        }

        boolean applyExtraModifier = defender.isTank() || defender.isVulture();

        return (defender.woundPercent() * (applyExtraModifier ? 2 : 1)) / 32.0;
    }

    // =========================================================

    private static double handleTerranInfantry(AUnit defender, AUnit attacker) {
        double criticalDist;

//        if (true) return 3;

        if (defender.hasMedicInRange()) {
            if (defender.isHealthy()) {
                defender.setTooltipTactical("Healthy");
                return enemyUnitsNearbyBonus(defender);
            }

            criticalDist = INFANTRY_BASE_IF_MEDIC
//                    + ourMovementBonus(defender)
//                    + enemyMovementBonus(defender, attacker)
                    + woundedAgainstMeleeBonus(defender, attacker);

//            if (defender.hp() >= 21) {
//                criticalDist = Math.min(criticalDist, 2.5);
//            }
            defender.setTooltipTactical("HasMedic");
        }

        // No medic nearby
        else {
            criticalDist = INFANTRY_BASE_IF_NO_MEDIC
//                    + ourMovementBonus(defender)
//                    + enemyMovementBonus(defender, attacker)
                    + workerBonus(defender, attacker)
                    + woundedAgainstMeleeBonus(defender, attacker);

//            if (
//                    defender.hp() >= 24
//                            && defender.friendsNearbyCount() >= 5
//                            && 4 * defender.friendsNearbyCount() >= defender.meleeEnemiesNearbyCount()
//            ) {
//                criticalDist = 1.7;
//            }

//                System.out.println("criticalDist = " + criticalDist + " (hp = " + defender.hp() + ")");

            criticalDist = Math.min(criticalDist, defender.isWounded() ? 3.2 : 2.5);

            String log = "NoMedic" + A.digit(criticalDist);
            defender.setTooltipTactical(log);
            defender.addLog(log);
        }

        criticalDist += enemyUnitsNearbyBonus(defender);
        criticalDist = Math.min(criticalDist, 3.5);

//        System.err.println("criticalDist against " + attacker + ": " + criticalDist + " // " + attacker.distTo(defender));
//        defender.addTooltip(A.digit(criticalDist));

        return criticalDist;
    }

}
