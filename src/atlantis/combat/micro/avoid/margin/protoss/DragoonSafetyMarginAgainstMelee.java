package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.game.A;
import atlantis.units.AUnit;

public class DragoonSafetyMarginAgainstMelee extends SafetyMarginAgainstMelee {
    public DragoonSafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    @Override
    public double marginAgainst(AUnit attacker) {
//        double base = 2.3
        double base = (defender.woundHp() >= 10 ? 3.0 : 1.1)

            + (defender.hp() <= 40 ? +0.5 : 0)
            + (defender.hp() <= 17 ? +0.5 : 0)
            + (defender.woundPercent() / 150.0)
            + (defender.isMoving() ? -0.2 : 0)
            + (defender.isAccelerating() ? -0.1 : 0)
            + (defender.lastAttackFrameMoreThanAgo(30 * 4) ? -1 : 0)
            + (defender.lastAttackFrameMoreThanAgo(30 * 6) ? -1 : 0)

//            + (attacker.isProtoss() ? +0.5 : 0)
            + (attacker.isMoving() ? +0.5 : 0)
            + ((attacker.hasTargetted(defender) || attacker.isFacing(defender)) ? 0.8 : -0.2);
//            + defender.woundPercent() / 300.0;

        if (!attacker.isDT()) {
            if (defender.isOtherUnitShowingBackToUs(attacker)) {
//                System.err.println("defender.isOtherUnitShowingBackToUs = ");
                base += defender.hp() >= 40 ? -2.0 : -0.7;
            }

//            if (quiteHealthyAndLongNotUnderAttack(attacker)) return 2.1;

            if (defender.shieldDamageAtMost(23)) {
                if (defender.friendsInRadiusCount(1.5) >= 3) {
                    base -= 0.5;
                }

                //            if (defender.lastUnderAttackMoreThanAgo(150) && defender.shieldDamageAtMost(16)) {
                //                return 0;
                //            }
            }
        }

        if (attacker.isZergling() && defender.isWounded()) {
            return base + (0.2);
        }

        if (defender.shieldDamageAtLeast(19)) {
            double minStillWhenCooldown = 2.6;
            if (base <= minStillWhenCooldown && defender.cooldown() >= 10) base = minStillWhenCooldown;
        }

//        System.err.println("@ " + A.now() + " - " + base);
        return Math.min(3.8, base);
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
