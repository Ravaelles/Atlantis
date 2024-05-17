package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.combat.retreating.protoss.ProtossTooBigBattleToRetreat;
import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class ZealotDontAvoidEnemy {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isZealot()) return false;

        Decision decision;

        if (healthyOrNotAttackedLong(unit)) return true;
        if ((decision = whenMissionDefend(unit)).notIndifferent()) return decision.toBoolean();

        return false;
    }

    private static boolean healthyOrNotAttackedLong(AUnit unit) {
        return unit.shieldDamageAtMost(9) || unit.lastAttackFrameMoreThanAgo(30 * 5);
    }

    private static Decision whenMissionDefend(AUnit unit) {
        if (!unit.isMissionDefend()) return Decision.INDIFFERENT;

        int tooManyEnemies = unit.shields() >= 40 ? 3 : 2;
        if (unit.enemiesNear().countInRadius(1.3, unit) >= tooManyEnemies) return Decision.FALSE;

        return Decision.INDIFFERENT;
    }
}
