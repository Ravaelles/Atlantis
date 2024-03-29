package atlantis.combat.micro.avoid.margin;

import atlantis.units.AUnit;

import static atlantis.units.AUnitType.Protoss_Zealot;
import static atlantis.units.AUnitType.Zerg_Devourer;

public class SafetyMarginAgainstMelee extends SafetyMargin {

    public static double INFANTRY_BASE_IF_MEDIC = 0;
    public static int INFANTRY_WOUND_MODIFIER_WITH_MEDIC = 19;
    public static double INFANTRY_BASE_IF_NO_MEDIC = 2.8;
    public static int INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC = 33;
    private static final double INFANTRY_CRITICAL_HEALTH_BONUS_IF_MEDIC = 1.95;
    private static final double INFANTRY_CRITICAL_HEALTH_BONUS_IF_NO_MEDIC = 3.0;

    public SafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    public double calculateAgainst(AUnit attacker) {
        double criticalDist = -1;

        // === Protoss ===============================================

        if ((criticalDist = forDragoon(attacker)) >= 0) {
            return criticalDist;
        }
        else if (defender.isDT()) {
            return 0;
        }

        // === Terran ===============================================

        else if (defender.isTerranInfantry()) {
            criticalDist = (new TerranSafetyMarginAgainstMelee(defender)).handleTerranInfantry(attacker);
        }

        // === Zerg ===============================================

        else if (defender.isHydralisk()) {
            criticalDist = forHydralisk(attacker);
        }

        // === Standard unit =========================================

        if (criticalDist == -1) {
            criticalDist = baseForMelee(attacker)
                    + enemyWeaponRange(attacker)
                    + woundedAgainstMeleeBonus(attacker)
                    + beastBonus(attacker)
                    + ourUnitsNearBonus(defender)
                    + workerBonus(attacker)
                    + ourMovementBonus(defender)
                    + quicknessBonus(attacker)
                    + enemyMovementBonus(attacker);

            // This should be enough as a minimum versus melee units
            criticalDist = Math.min(criticalDist, defender.isDragoon() ? 2.95 : 3.4);
        }
        else {
            criticalDist += beastBonus(attacker);
        }

        if (defender.isRanged() && attacker.isWorker()) {
            criticalDist = 2.3;
        }

        return criticalDist;
    }

    private double forHydralisk(AUnit attacker) {
        if (!defender.isHydralisk()) {
            return -1;
        }

        if (defender.isHealthy()) {
            return 0;
        }

        return defender.woundPercent() / 33;
    }

    protected double enemyFacingThisUnitBonus(AUnit attacker) {
        if (defender.isOtherUnitFacingThisUnit(attacker)) {
            return -0.3;
        }

        return +0.2;
    }

    private double forDragoon(AUnit attacker) {
        if (!defender.isDragoon()) {
            return -1;
        }

        if (defender.shieldDamageAtMost(23) && !attacker.isDT()) {
            if (defender.friendsInRadiusCount(1.5) >= 3) {
                return 1.1;
            }

            if (defender.cooldownRemaining() <= 3) {
                return 0;
            }

//            if (defender.lastUnderAttackMoreThanAgo(150) && defender.shieldDamageAtMost(16)) {
//                return 0;
//            }
        }

        if (attacker.isZergling()) {
            return (0.2 + defender.woundPercent() / 40);
        }

        return -1;

//        double base = woundedAgainstMeleeBonus(attacker)
//            + beastBonus(defender);
//        boolean enemyFacingUs = defender.isOtherUnitFacingThisUnit(attacker);
//
//        if (attacker.isWorker() && attacker.hp() >= 30) {
//            base += 0.5;
//        }
//
//        else if (!enemyFacingUs && defender.shieldDamageAtMost(40)) {
//            base -= 1.5;
//        }
//
//        else if (
//                (attacker.hp() <= 16 || defender.shieldDamageAtMost(38))
//                        && (
//                        !enemyFacingUs
//                                || defender.lastAttackFrameMoreThanAgo(130)
//                                || (defender.lastAttackFrameMoreThanAgo(90) && attacker.hpPercent(30))
//                                || (defender.lastAttackFrameMoreThanAgo(40) && defender.lastUnderAttackMoreThanAgo(150))
//                )
//        ) {
//            defender.addLog("CoolDragoon_" + defender.lastAttackFrameAgo());
//            base += (defender.isHealthy() ? 0 : 0.3);
//            //            criticalDist = handleDragoon(attacker);
//        }
//
//
//        // =========================================================
//
//        base = Math.max(0, base);
//        base = Math.min(defender.isMissionDefend() ? (1 + defender.woundPercent() / 100) : 2.4, base);
//        return base;
//
////        if (Missions.isGlobalMissionDefend()) {
////            return base;
////        }
////
////        return -1;
    }

    // =========================================================

//    private double handleDragoon(AUnit attacker) {
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
//                    + woundedAgainstMeleeBonus(attacker)
//                    + ourMovementBonus(defender)
//                    + quicknessBonus(attacker)
//                    + enemyMovementBonus(attacker);
//        }
////        else if (defender.hp() >= 21 && defender.lastAttackFrameMoreThanAgo(30 * 3)) {
////            return safe;
////        }
//
//        return -1;
//    }

    // =========================================================

    private double enemyMeleeUnitsNearBonus(AUnit defender) {
        if (defender.meleeEnemiesNearCount() >= 2) {
            return 1.8;
        }

        return 0;
    }

    private double baseForMelee(AUnit attacker) {
        return attacker.isZealot() ? 0.5 : 0.7;
    }

    protected double enemyUnitsNearBonus(AUnit defender) {
        if (defender.enemiesNear().ofType(Protoss_Zealot).inRadius(2, defender).atLeast(3)) {
            return 2.3;
        }

        if (defender.enemiesNear().ofType(Protoss_Zealot).inRadius(2, defender).atLeast(2)) {
            return 1.6;
        }

        return 0;
    }

    protected double beastBonus(AUnit attacker) {
        if (defender.isAir() && attacker.is(Zerg_Devourer)) {
            return 1.2;
        }

        if (attacker.isDT() && attacker.distToLessThan(defender, 4.4)) {
            return 2.6;
        }

        if ((attacker.isUltralisk() || attacker.isArchon()) && defender.distToLessThan(attacker, 4.4)) {
            return 1.0 + (defender.hasCooldown() ? 0.7 : 0);
        }

        return 0;
    }

    protected double woundedAgainstMeleeBonus(AUnit attacker) {
//        if (attacker.isRanged()) {
//            return 2;
//        }

        if (defender.isZealot()) {
            return defender.hpLessThan(21) ? 1.8 : 0;
        }

        if (defender.isTerranInfantry()) {
            // Medic in range
            if (defender.hasMedicInRange()) {
                if (defender.hp() <= 18) {
                    return INFANTRY_CRITICAL_HEALTH_BONUS_IF_MEDIC;
                }
                return defender.woundPercent() / INFANTRY_WOUND_MODIFIER_WITH_MEDIC;
            }
            // No medic in range
            else {
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
}
