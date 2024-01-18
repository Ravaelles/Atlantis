package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.units.AUnit;

public class DragoonSafetyMarginAgainstMelee extends SafetyMarginAgainstMelee {
    public DragoonSafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    @Override
    public double marginAgainst(AUnit attacker) {
        double cooldownBonus = defender.cooldownRemaining() <= 5 ? 0.7 : 0;
        double base = cooldownBonus;

        if (defender.shieldDamageAtMost(23) && !attacker.isDT()) {
            if (defender.friendsInRadiusCount(1.5) >= 3) {
                return base + 1.1;
            }

//            if (defender.lastUnderAttackMoreThanAgo(150) && defender.shieldDamageAtMost(16)) {
//                return 0;
//            }
        }

        if (attacker.isZergling()) {
            return base + (0.2 + defender.woundPercent() / 45.0);
        }

        return -1;
    }
}
