package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstRanged;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;

public class DragoonSafetyMarginAgainstRanged extends SafetyMarginAgainstRanged {
    public DragoonSafetyMarginAgainstRanged(AUnit defender) {
        super(defender);
    }

    @Override
    public double marginAgainst(AUnit attacker) {
        if (attacker.hasBiggerWeaponRangeThan(defender)) return -0.1;
        if ((new KeepDragoonBattleLineVsRanged(defender, attacker)).dontSeparateVsRanged()) return -0.1;

        if (
            defender.cooldown() <= 11
                && defender.hp() >= 23
                && defender.eval() >= 0.95
        ) return -0.1;

        if (defender.isOtherUnitShowingBackToUs(attacker)) return -0.1;

        double criticalDist = 0
            + enemyWeaponRange(attacker)
            + woundedBonus(attacker)
            + (defender.cooldown() >= 13 ? +1.0 : 0)
            + (defender.woundHp() >= 39 && defender.cooldown() >= 9 ? +0.5 : 0)
            + (defender.lastUnderAttackLessThanAgo(20) ? 1.0 : 0)
            + ourNotMovingPenalty(defender);
//            + enemyFacingDirectionBonus(attacker);

//        System.err.println("criticalDist = " + criticalDist);

        return Math.max(OurDragoonRange.range() - 0.1, criticalDist);
    }

    public double woundedBonus(AUnit attacker) {
        double bonus = defender.woundPercent() / 47.0;

        if (defender.hp() <= 50 && defender.lastUnderAttackLessThanAgo(30)) {
            bonus += 1;
        }

        return bonus;
    }

//    private double enemyFacingDirectionBonus(AUnit attacker) {
//        if (defender.isOtherUnitShowingBackToUs(attacker)) return -2.5;
//        if (attacker.isMoving() && defender.isOtherUnitFacingThisUnit(attacker)) return +0.5;
//
//        return 0;
//    }
}
