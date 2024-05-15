package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.units.AUnit;

public class DragoonSafetyMarginAgainstMelee extends SafetyMarginAgainstMelee {
    public DragoonSafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    @Override
    public double marginAgainst(AUnit attacker) {
//        double base = 2.3
        double base = (defender.isWounded() ? 3.0 : 1.1)
//            + cooldownBonus(attacker)
            + (defender.isMoving() ? -0.3 : 0)
            + (defender.isAccelerating() ? -0.1 : 0)
            + (attacker.isMoving() ? +0.5 : 0)
            + (attacker.isFacing(defender) ? 0.7 : -0.2);
//            + defender.woundPercent() / 300.0;

        if (!attacker.isDT()) {
            if (attacker.isOtherUnitShowingBackToUs(defender)) {
                return defender.hp() >= 40 ? 0 : 1.7;
            }

//            if (quiteHealthyAndLongNotUnderAttack(attacker)) return 2.1;

            if (defender.shieldDamageAtMost(23)) {
                if (defender.friendsInRadiusCount(1.5) >= 3) {
                    return base + 1.1;
                }

                //            if (defender.lastUnderAttackMoreThanAgo(150) && defender.shieldDamageAtMost(16)) {
                //                return 0;
                //            }
            }
        }

        if (attacker.isZergling() && defender.isWounded()) {
            return base + (0.2);
        }

        return -1;
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
