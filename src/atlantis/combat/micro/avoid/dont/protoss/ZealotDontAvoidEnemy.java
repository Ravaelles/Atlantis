package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.decisions.Decision;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class ZealotDontAvoidEnemy {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isZealot()) return false;

        Decision decision;
        if ((decision = whenMissionDefend(unit)).notIndifferent()) return decision.toBoolean();

        if (earlyZealotAvoidBecauseAlmostDeadOrOverpowered(unit)) return false;

        if ((decision = whenGoonsFightingNearby(unit)).notIndifferent()) return decision.toBoolean();
        if (healthyOrNotAttackedLong(unit)) return true;

        return false;
    }

    private static Decision whenGoonsFightingNearby(AUnit unit) {
        if (unit.hp() <= 25) return Decision.FALSE;
        if (unit.cooldown() >= 5) return Decision.FALSE;

        Selection goons = unit.friendsNear().dragoons().notRunning().inRadius(4, unit);

        if (goons.atLeast(2)) return Decision.TRUE;

        return Decision.INDIFFERENT;
    }

    private static boolean earlyZealotAvoidBecauseAlmostDeadOrOverpowered(AUnit unit) {
        if (!unit.isZealot()) return false;
        if (!unit.isMissionDefend()) return false;

        return Count.ourCombatUnits() <= 10
            && (unit.hp() <= 33 || unit.eval() < 0.8)
            && unit.isTargetRanged()
            && unit.distToTarget() <= 2
            && unit.friendsNear().ranged().inRadius(4.5, unit).atLeast(1);
    }

    private static boolean healthyOrNotAttackedLong(AUnit unit) {
        return unit.shieldDamageAtMost(9) || unit.lastAttackFrameMoreThanAgo(30 * 5);
    }

    private static Decision whenMissionDefend(AUnit unit) {
        if (!unit.isMissionDefendOrSparta()) return Decision.INDIFFERENT;

        HasPosition safety = unit.safetyPosition();
        if (safety != null && unit.noCooldown() && safety.distTo(unit) <= 2) {
            return Decision.TRUE;
        }

        int tooManyEnemies = unit.shields() >= 40 ? 3 : 2;
        if (unit.enemiesNear().countInRadius(1.4, unit) >= tooManyEnemies) return Decision.FALSE;

        return Decision.INDIFFERENT;
    }
}
