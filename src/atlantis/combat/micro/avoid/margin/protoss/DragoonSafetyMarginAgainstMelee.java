package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;
import atlantis.util.We;

import static bwapi.Color.Red;

public class DragoonSafetyMarginAgainstMelee extends SafetyMarginAgainstMelee {
    public DragoonSafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    @Override
    public double marginAgainst(AUnit attacker) {
        if (attacker.hasBiggerWeaponRangeThan(defender)) return -1;

        double margin = baseValueAgainst(attacker)
            + (defender.woundPercent() / 62.0)
            + (defender.cooldown() >= 14 ? +0.4 : 0)
            + (defender.cooldown() >= 6 ? +0.4 : 0);

        margin = Math.min(OurDragoonRange.range() - 0.3, margin);

//        System.err.println("@" + A.now + " safetyMargin = " + margin + " " + defender.digitDistTo(attacker));
//        defender.paintCircle((int) (margin * 32), Red);
//        defender.paintCircle((int) (margin * 32) + 1, Red);

        return margin;

    }

    private double baseValueAgainst(AUnit attacker) {
        boolean lookingAtUs = attacker.isTarget(defender) || defender.isOtherUnitFacingThisUnit(attacker);

        if (!lookingAtUs) return 0.5;

        if (attacker.isZealot()) return baseVsZealot();
        if (attacker.isZergling()) return baseVsZergling();

        return 2.3;
    }

    private double baseVsZealot() {
        return 2.3
            + (defender.meleeEnemiesNearCount(3) >= 2 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(3.5) >= 3 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(3.5) >= 4 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(3.8) >= 3 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(4.5) >= 5 ? 0.4 : 0);
    }

    private double baseVsZergling() {
        double base = 2.08;
        int meleeEnemiesNear = defender.meleeEnemiesNearCount(3.5);

        if (defender.cooldown() >= 4) {
            base += (meleeEnemiesNear >= 3 ? 0.4 : 0)
                + (meleeEnemiesNear >= 4 ? 0.4 : 0);
        }

        return base
            + (defender.cooldown() >= 10 ? 2.0 : 0)
            + (
            defender.shotSecondsAgo() >= 1.5
                && meleeEnemiesNear <= (defender.shields() >= 10 ? 2 : 1)
                ? -1.5 : 0
        );
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
