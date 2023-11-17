package atlantis.combat.micro.avoid.margin.terran;

import atlantis.units.AUnit;

public class BonusForWraith {
    public static double bonusForWraith(AUnit attacker, AUnit defender) {
        if (defender.effUndetected()) return 0;

        double base = 1.3
            + (defender.enemiesNear().detectors().inRadius(13, defender).notEmpty() ? 2 : 0);

        if (attacker.isDragoon()) {
            return base
                + (defender.isOtherUnitFacingThisUnit(attacker) ? 1.7 : -0.4)
                + (defender.woundPercent() / 50.0)
                + (defender.lastUnderAttackLessThanAgo(150) ? 0.8 : 0);
        }

        return base;
    }
}
