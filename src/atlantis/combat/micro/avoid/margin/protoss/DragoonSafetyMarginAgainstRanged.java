package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstRanged;
import atlantis.units.AUnit;

public class DragoonSafetyMarginAgainstRanged extends SafetyMarginAgainstRanged {
    public DragoonSafetyMarginAgainstRanged(AUnit defender) {
        super(defender);
    }

    @Override
    public double marginAgainst(AUnit attacker) {
        if (attacker.hasBiggerWeaponRangeThan(defender)) return -0.1;

        if (defender.cooldown() <= 5 && defender.combatEvalRelative() >= 1.2) return 0;

        double criticalDist = 0
            + enemyWeaponRange(attacker)
            + woundedBonus(attacker)
            + (defender.lastUnderAttackLessThanAgo(40) ? 0.8 : 0)
            + ourNotMovingPenalty(defender)
            + enemyFacingDirectionBonus(attacker);

//        System.err.println("criticalDist = " + criticalDist);

        return criticalDist;
    }

    public double woundedBonus(AUnit attacker) {
        double bonus = defender.woundPercent() / 22.0;

        if (defender.hp() <= 50 && defender.lastUnderAttackLessThanAgo(30)) {
            bonus += 1;
        }

        return bonus;
    }

    private double enemyFacingDirectionBonus(AUnit attacker) {
        if (defender.isOtherUnitShowingBackToUs(attacker)) return -2.5;
        if (attacker.isMoving() && defender.isOtherUnitFacingThisUnit(attacker)) return +0.5;

        return 0;
    }
}
