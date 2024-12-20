package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;

import static bwapi.Color.Red;

public class DragoonSafetyMarginAgainstMeleeOld extends SafetyMarginAgainstMelee {
    public DragoonSafetyMarginAgainstMeleeOld(AUnit defender) {
        super(defender);
    }

    @Override
    public double marginAgainst(AUnit attacker) {
        if (attacker.hasBiggerWeaponRangeThan(defender)) return -1;

//        if (true) return 2.2 + (defender.cooldown() >= 5 ? +0.3 : 0);

//        double base = 2.3
        double base = 0

            + (defender.hp() <= 40 ? +0.5 : 0)
            + (defender.hp() <= 17 ? +0.7 : 0)
            + (defender.woundPercent() / 60.0)
            + (defender.cooldown() >= 5 ? +1.0 : 0)
//            + (defender.cooldown() >= 4 ? +0.2 : 0)

//            + (defender.isMoving() ? 0 : 0.3)
            + (attacker.isMoving() ? 0.4 : 0)

            + enemyFacingThisUnitBonus(attacker);

//            + (defender.isAccelerating() ? -0.1 : 0)
//            + (defender.lastAttackFrameMoreThanAgo(30 * 4) ? -1 : 0)
//            + (defender.lastAttackFrameMoreThanAgo(30 * 6) ? -1 : 0)

//            + (attacker.isProtoss() ? +0.5 : 0)
//            + ((attacker.hasTargetted(defender) || attacker.isFacing(defender)) ? 0.8 : -0.2);
//            + defender.woundPercent() / 300.0;

        if (attacker.isDT()) {
            base += defender.hp() >= 50 ? 0.7 : 2.0;
//            if (defender.isOtherUnitShowingBackToUs(attacker)) {
////                System.err.println("defender.isOtherUnitShowingBackToUs = ");
//                base += defender.hp() >= 40 ? -2.0 : -0.7;
//            }
//
////            if (quiteHealthyAndLongNotUnderAttack(attacker)) return 2.1;
//
//            if (defender.shieldDamageAtMost(23)) {
//                if (defender.friendsInRadiusCount(1.5) >= 3) {
//                    base -= 0.5;
//                }
//
//                //            if (defender.lastUnderAttackMoreThanAgo(150) && defender.shieldDamageAtMost(16)) {
//                //                return 0;
//                //            }
//            }
        }

        if (attacker.isZergling() && defender.isWounded()) {
            return base + (0.2);
        }

        if (defender.shieldDamageAtLeast(19)) {
            double minStillWhenCooldown = 2.9;
            if (base <= minStillWhenCooldown && defender.cooldown() >= 10) base = minStillWhenCooldown;
        }

//        System.err.println("@ " + A.now() + " - " + base);
//        return Math.min(3.8, base);
        double margin = Math.min(OurDragoonRange.range() - 0.25, base);

//        System.err.println("@" + A.now + " safetyMargin = " + margin + " " + defender.digitDistTo(attacker));
        defender.paintCircle((int) (margin * 32), Red);
        defender.paintCircle((int) (margin * 32) + 1, Red);

        return margin;
    }

    @Override
    protected double enemyFacingThisUnitBonus(AUnit attacker) {
        double BASE = 1.0;

        if (attacker.isTarget(defender)) {
//                defender.paintCircleFilled(12, Color.Red);
//                System.out.println(A.fr + " DefenderTargetted ");
            return BASE + 0.1;
//                return 3.2;
        }

        if (defender.isOtherUnitFacingThisUnit(attacker)) {
//                defender.paintCircleFilled(12, Color.Orange);
            return BASE;
        }

        if (defender.isOtherUnitShowingBackToUs(attacker)) {
//            defender.paintCircleFilled(12, Color.Green);
            return -1.9;
        }

        return 0;
    }

    private boolean quiteHealthyAndLongNotUnderAttack(AUnit attacker) {
        return defender.hp() >= 100
            && defender.lastUnderAttackMoreThanAgo(30 * 6);
    }

    private double cooldownBonus(AUnit attacker) {
//        return defender.cooldownRemaining() >= 10 ? (defender.cooldownRemaining() / 10.0) : 0;
        return defender.cooldownRemaining() >= 8 ? (defender.cooldownRemaining() / 10.0) : 0;
    }
}
