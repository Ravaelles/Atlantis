package atlantis.combat.micro.avoid.margin.terran;

import atlantis.units.AUnit;

public class BonusForWraith {
    public static double bonusForWraith(AUnit attacker, AUnit defender) {
        if (defender.effUndetected()) return 0;

        if (attacker.isDragoon()) {
            return 1.3
                + (defender.isOtherUnitFacingThisUnit(attacker) ? 1.7 : -0.4)
                + (defender.isWounded() ? 0.8 : 0)
                + (defender.lastUnderAttackLessThanAgo(150) ? 0.8 : 0);
        }

        return 1.3;
    }
}
