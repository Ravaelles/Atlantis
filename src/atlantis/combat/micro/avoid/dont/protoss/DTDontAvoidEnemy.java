package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class DTDontAvoidEnemy {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isDT()) return false;
        if (unit.effVisible()) return false;
        if (unit.lastUnderAttackLessThanAgo(90)) return false;

        return unit.enemiesNear().detectors().inRadius(12.5, unit).notEmpty();
    }

    private static boolean earlyZealotAvoidBecauseAlmostDeadOrOverpowered(AUnit unit) {
        if (!unit.isZealot()) return false;
        if (!unit.isMissionDefend()) return false;

        return Count.ourCombatUnits() <= 10
            && (unit.hp() <= 33 || unit.combatEvalRelative() < 0.8);
    }

    private static boolean healthyOrNotAttackedLong(AUnit unit) {
        return unit.shieldDamageAtMost(9) || unit.lastAttackFrameMoreThanAgo(30 * 5);
    }

    private static Decision whenMissionDefend(AUnit unit) {
        if (!unit.isMissionDefendOrSparta()) return Decision.INDIFFERENT;

        int tooManyEnemies = unit.shields() >= 40 ? 3 : 2;
        if (unit.enemiesNear().countInRadius(1.4, unit) >= tooManyEnemies) return Decision.FALSE;

        return Decision.INDIFFERENT;
    }
}
