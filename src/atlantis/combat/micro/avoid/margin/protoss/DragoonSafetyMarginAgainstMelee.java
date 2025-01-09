package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;

import static bwapi.Color.Red;

public class DragoonSafetyMarginAgainstMelee extends SafetyMarginAgainstMelee {
    public DragoonSafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    @Override
    public double marginAgainst(AUnit attacker) {
        if (attacker.hasBiggerWeaponRangeThan(defender)) return -1;

        if (defender.shieldWound() <= 9 && attacker.isZergling() && defender.cooldown() <= 5) return -0.1;

//        System.out.println("A = " + A.digit((defender.woundPercent() / 90.0)));
        double margin = baseValueAgainst(attacker)
            + (defender.woundPercent() / 120.0)
//            + (defender.cooldown() >= 8 ? +0.8 : 0)
//            + (defender.cooldown() >= 14 ? +1 : 0)
            + (defender.cooldown() >= 9 ? +3.5 : 0)
            + enemyShowingBackBonus(attacker)

            + (defender.meleeEnemiesNearCount(3.2) >= 2 ? +0.5 : 0)
            + (defender.meleeEnemiesNearCount(3.2) >= 3 ? +1.5 : 0);

        margin = Math.min(OurDragoonRange.range() - 0.17, margin);

//        System.err.println(A.at() + ": safetyMargin = " + margin + " " + defender.distToDigit(attacker));
        defender.paintCircle((int) (margin * 32), Red);
        defender.paintCircle((int) (margin * 32) + 1, Red);

        return margin;

    }

    private double baseVsZealot(AUnit attacker) {
//        System.out.println(
//            "Moving: " + (attacker.isMoving() ? 0 : -0.7)
//            + " / Facing: " + (defender.isOtherUnitFacingThisUnit(attacker) ? +0 : -0.15)
//        );

        return 2.25

            + (attacker.isMoving() ? 0 : -0.3)
            + (defender.isOtherUnitFacingThisUnit(attacker) ? 0 : -0.1)
            + (defender.shieldWound() <= 6 ? -0.2 : 0)

            + (defender.meleeEnemiesNearCount(2.5) >= 2 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(3.0) >= 3 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(3.2) >= 4 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(3.4) >= 3 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(4.5) >= 5 ? 0.4 : 0);
    }

    private double baseVsZergling(AUnit attacker) {
        double base = 2.08;
        int meleeEnemiesNear = defender.meleeEnemiesNearCount(3.5);
        boolean hasCooldown = defender.cooldown() >= 4;

        base += (meleeEnemiesNear >= 3 ? (hasCooldown ? 0.4 : 0.15) : 0)
            + (meleeEnemiesNear >= 4 ? (hasCooldown ? 0.4 : 0.15) : 0);

        return base
//            + (defender.cooldown() >= 10 ? 2.0 : 0)
            + (
            defender.shotSecondsAgo() >= 1.5
                && meleeEnemiesNear <= (defender.shields() >= 10 ? 2 : 1)
                ? -1.5 : 0
        );
    }

    private double baseValueAgainst(AUnit attacker) {
//        boolean lookingAtUs = attacker.isTarget(defender) || defender.isOtherUnitFacingThisUnit(attacker);

        if (attacker.isDT()) return 2.6;

//        if (!lookingAtUs) {
////            System.err.println(A.minSec() + " - NOT LOOKING AT US");
////            attacker.paintCircleFilled(12, Yellow);
//            return defender.isOtherUnitShowingBackToUs(attacker) ? 0.9 : 2.3;
//        }

        if (attacker.isZealot()) return baseVsZealot(attacker);
        if (attacker.isZergling()) return baseVsZergling(attacker);

        return 2.3;
    }

    private double enemyShowingBackBonus(AUnit attacker) {
        if (defender.hp() >= 19 && defender.isOtherUnitShowingBackToUs(attacker)) return -5;

        return 0;
    }

//    @Override
//    protected double enemyFacingThisUnitBonus(AUnit attacker) {
//        double BASE = 1.0;
//
//        if (attacker.isTarget(defender)) {
////                defender.paintCircleFilled(12, Color.Red);
////                System.out.println(A.fr + " DefenderTargetted ");
//            return BASE + 0.1;
////                return 3.2;
//        }
//
//        if (defender.isOtherUnitFacingThisUnit(attacker)) {
////                defender.paintCircleFilled(12, Color.Orange);
//            return BASE;
//        }
//
//        if (defender.isOtherUnitShowingBackToUs(attacker)) {
////            defender.paintCircleFilled(12, Color.Green);
//            return -1.9;
//        }
//
//        return 0;
//    }

//    private boolean quiteHealthyAndLongNotUnderAttack(AUnit attacker) {
//        return defender.hp() >= 100
//            && defender.lastUnderAttackMoreThanAgo(30 * 6);
//    }
//
//    private double cooldownBonus(AUnit attacker) {
////        return defender.cooldownRemaining() >= 10 ? (defender.cooldownRemaining() / 10.0) : 0;
//        return defender.cooldownRemaining() >= 8 ? (defender.cooldownRemaining() / 10.0) : 0;
//    }
}
