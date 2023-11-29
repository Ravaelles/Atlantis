package atlantis.combat.micro.avoid.margin.terran;

import atlantis.units.AUnit;

public class BonusForWraith {
    public static double bonusForWraith(AUnit attacker, AUnit defender) {
        if (defender.effUndetected()) return 0;

        double base = 1.3
            + (defender.enemiesNear().detectors().inRadius(13, defender).notEmpty() ? 2 : 0)
            + (defender.woundPercent() / 20.0)
            + (defender.enemiesNear().combatBuildingsAntiAir().inRadius(13, defender).notEmpty() ? 1.5 : 0);

        if (attacker.isGroundUnit() && attacker.isRanged()) {
            return base
                + (defender.isOtherUnitFacingThisUnit(attacker) ? 1.7 : -0.4)
                + (defender.lastUnderAttackLessThanAgo(200) ? 1.7 : 0);
        }

        return base;
    }
}
