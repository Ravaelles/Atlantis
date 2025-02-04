package atlantis.combat.micro.avoid.margin.terran;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.range.OurMarineRange;

public class MarineSafetyMarginAgainstMelee extends SafetyMarginAgainstMelee {
    public MarineSafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    @Override
    public double marginAgainst(AUnit attacker) {
//        if (attacker.hasBiggerWeaponRangeThan(defender)) return -1;

        boolean lookingAtUs = attacker.isTarget(defender) || defender.isOtherUnitFacingThisUnit(attacker);
        if (!lookingAtUs) return defender.isOtherUnitShowingBackToUs(attacker) ? 0.2 : 1.4;

        double margin = baseValueAgainst(attacker)
            + (defender.woundPercent() / 76.0)
            + (defender.cooldown() >= 13 ? +0.4 : 0)
            + (defender.cooldown() >= 5 ? +0.4 : 0)
            + manyEnemiesNearBonus(defender);

        margin = Math.min(OurMarineRange.range() - 0.25, margin);

//        System.err.println("@" + A.now + " safetyMargin = " + margin + " " + defender.digitDistTo(attacker));
//        defender.paintCircle((int) (margin * 32), Red);
//        defender.paintCircle((int) (margin * 32) + 1, Red);

        return margin;

    }

    private double manyEnemiesNearBonus(AUnit defender) {
        int meleeEnemiesNearCount = defender.meleeEnemiesNearCount(4.5);

        if (meleeEnemiesNearCount >= 3) return 1.4;
        if (meleeEnemiesNearCount >= 2) return 0.4;

        return 0;
    }

    private double baseValueAgainst(AUnit attacker) {
        if (attacker.isZealot()) return baseVsZealot();
        if (attacker.isZergling()) return baseVsZergling();

        return 2.1;
    }

    private double baseVsZealot() {
        return 2.0
            + (defender.meleeEnemiesNearCount(3) >= 2 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(3.5) >= 3 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(3.5) >= 4 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(3.8) >= 3 ? 0.4 : 0)
            + (defender.meleeEnemiesNearCount(4.5) >= 5 ? 0.4 : 0);
    }

    private double baseVsZergling() {
//        double base = 2.28;
        double base = 1.38;
        int meleeEnemiesNear = defender.meleeEnemiesNearCount(3.5);

        if (defender.cooldown() >= 4) {
            base += 1.2
                + (meleeEnemiesNear >= 3 ? 0.4 : 0)
                + (meleeEnemiesNear >= 4 ? 0.4 : 0);
        }

        return base
            + (defender.cooldown() >= 10 ? 2.0 : 0)
            + (
            ((defender.shotSecondsAgo() >= 1.5 && meleeEnemiesNear <= 2) ? -1.5 : 0)
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
