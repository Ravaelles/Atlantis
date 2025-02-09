package atlantis.combat.micro.avoid.margin;

import atlantis.combat.micro.avoid.margin.protoss.ProtossSafetyMarginAgainstMelee;
import atlantis.combat.micro.avoid.margin.protoss.ZealotSafetyMarginAgainstMelee;
import atlantis.combat.micro.avoid.margin.special.SafetyMarginAgainstMelee_Special;
import atlantis.combat.micro.avoid.margin.terran.FirebatSafetyMarginAgainstMelee;
import atlantis.combat.micro.avoid.margin.terran.MarineSafetyMarginAgainstMelee;
import atlantis.combat.micro.avoid.margin.zerg.ZergSafetyMarginAgainstMelee;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.We;

import static atlantis.units.AUnitType.Protoss_Zealot;
import static atlantis.units.AUnitType.Zerg_Devourer;

public class SafetyMarginAgainstMelee extends SafetyMargin {
    public static double INFANTRY_BASE_IF_MEDIC = 0;
    public static int INFANTRY_WOUND_MODIFIER_WITH_MEDIC = 19;
    public static double INFANTRY_BASE_IF_NO_MEDIC = 2.95;
    public static int INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC = 33;
    //    private static final double INFANTRY_CRITICAL_HEALTH_BONUS_IF_MEDIC = 1.95;
    private static final double INFANTRY_CRITICAL_HEALTH_BONUS_IF_MEDIC = 2.1;
    private static final double INFANTRY_CRITICAL_HEALTH_BONUS_IF_NO_MEDIC = 3.0;

    public SafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    public double marginAgainst(AUnit attacker) {
        double criticalDist = -1;

        SafetyMarginAgainstMelee_Special special = new SafetyMarginAgainstMelee_Special(defender);
        double specialMargin = special.handleSpecially(attacker);
        if (specialMargin >= 0) return specialMargin;

        // === Protoss ===============================================

        if (defender.isProtoss()) {
            if (We.protoss()) {
                if (defender.isZealot()) {
                    double margin = (new ZealotSafetyMarginAgainstMelee(defender)).marginAgainst(attacker);
                    if (margin > -1) return margin;
                }
            }

            criticalDist = (new ProtossSafetyMarginAgainstMelee(defender)).handle(attacker);
        }

        // === Terran ===============================================

        else if (defender.isTerranInfantry()) {
            if (defender.isMarine()) {
                double margin = (new MarineSafetyMarginAgainstMelee(defender)).marginAgainst(attacker);
                if (margin > -1) return margin;
            }
            else if (defender.isFirebat()) {
                double margin = (new FirebatSafetyMarginAgainstMelee(defender)).marginAgainst(attacker);
                if (margin > -1) return margin;
            }

            criticalDist = (new TerranSafetyMarginAgainstMelee(defender)).handleTerranInfantry(attacker);
        }

        // === Zerg ===============================================

        else if (defender.isZerg()) {
            criticalDist = (new ZergSafetyMarginAgainstMelee(defender)).handle(attacker);
        }

        // === Standard unit =========================================

        if (criticalDist == -1) {
            criticalDist = baseAgainstMelee(attacker)
                + enemyWeaponRange(attacker)
                + woundedAgainstMeleeBonus(attacker)
                + beastBonus(attacker)
                + ourUnitsNearBonus(defender)
                + asWorkerBonus(attacker)
                + ourNotMovingPenalty(defender)
                + quicknessBonus(attacker)
                + enemyMovementBonus(attacker);

            // This should be enough as a minimum versus melee units
//            criticalDist = Math.min(criticalDist, defender.isDragoon() ? 2.95 : 3.4);
            criticalDist = Math.min(criticalDist, defender.isDragoon() ? 3.6 : 3.4);
        }
        else {
            criticalDist += beastBonus(attacker);
        }

        if (defender.isRanged() && attacker.isWorker()) {
            criticalDist = 2.3;
        }

//        if (defender.isActiveManager(ContinueShotAnimation.class) && defender.lastAttackFrameMoreThanAgo(24)) {
//            A.printStackTrace("How comes we wanna avoid?");
//            defender.manager().printParentsStack();
//        }

//        System.err.println("@" + A.now + " safetyMargin = " + criticalDist + " " + defender.distToDigit(attacker));

        return criticalDist;
    }

    protected double enemyFacingThisUnitBonus(AUnit attacker) {
        if (defender.isWounded() || (We.terran() && !defender.hasMedicInRange())) {
            if (attacker.isTarget(defender)) {
//                defender.paintCircleFilled(12, Color.Red);
//                System.out.println(A.fr + " DefenderTargetted ");
                return 2.5;
//                return 3.2;
            }

            if (defender.isOtherUnitFacingThisUnit(attacker)) {
//                defender.paintCircleFilled(12, Color.Orange);
                return 1.4;
            }
        }

        if (defender.isOtherUnitShowingBackToUs(attacker)) {
//            defender.paintCircleFilled(12, Color.Green);
            return -1.9;
        }

        return 0;
    }

//    private double forDragoon(AUnit attacker) {
//        if (!defender.isDragoon()) return -1;
//
//        double base = 0;
//
//        double cooldownBonus = defender.cooldownRemaining() <= 5 ? 0.7 : 0;
//
//        if (defender.shieldDamageAtMost(23) && !attacker.isDT()) {
//            if (defender.friendsInRadiusCount(1.5) >= 3) {
//                return 1.1;
//            }
//
//            if (true) {
//                return 0;
//            }
//
////            if (defender.lastUnderAttackMoreThanAgo(150) && defender.shieldDamageAtMost(16)) {
////                return 0;
////            }
//        }
//
//        if (attacker.isZergling()) {
//            return (0.2 + defender.woundPercent() / 40);
//        }
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

    private double baseAgainstMelee(AUnit attacker) {
        double base = 0.7;

        if (attacker.isZealot()) {
            if (We.protoss()) base = 0.5;
            else if (We.zerg()) base = 1.0;
            else if (We.terran()) base = 0.5;
        }

        if (defender.isVulture()) base += 0.5;

        return base;
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
        if (defender.isHealthy()) return 0;

        if (defender.isDragoon()) {
            return defender.woundPercent() / 33.0;
        }

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
}
