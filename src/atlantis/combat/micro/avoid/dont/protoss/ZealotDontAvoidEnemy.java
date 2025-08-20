package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class ZealotDontAvoidEnemy {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isZealot()) return false;
        if (unit.isMissionSparta() && unit.distToFocusPoint() <= 2 && unit.hp() >= 18) return true;
        if (dontAvoidWhenCloseEnemyWorkers(unit)) return true;

        if (earlyZealotAvoidBecauseAlmostDead(unit)) return false;

        Decision decision;

        if (Enemy.protoss()) {
            if ((decision = whenEnemyGoonNearby(unit)).notIndifferent()) return decision.toBoolean();
        }

        if (Enemy.zerg()) {
            if ((decision = whenEarlyDefendingChokeVZ(unit)).notIndifferent()) return decision.toBoolean();
        }

        if ((decision = whenWorkersFightingNearby(unit)).notIndifferent()) return decision.toBoolean();
        if ((decision = whenGoonsFightingNearby(unit)).notIndifferent()) return decision.toBoolean();
        if ((decision = whenMissionDefendDontAvoid(unit)).notIndifferent()) return decision.toBoolean();

        if (healthyOrNotAttackedLong(unit)) return true;

        return false;
    }

    private static boolean dontAvoidWhenCloseEnemyWorkers(AUnit unit) {
        if (!unit.isMissionAttackOrGlobalAttack()) return false;

        Selection workers = unit.enemiesNear().workers();
        if (workers.empty()) return false;

        if (workers.countInRadius(1.2, unit) > 0 && unit.hp() >= 42 && unit.eval() >= 0.7) return true;

        return false;
    }

    private static Decision whenEnemyGoonNearby(AUnit unit) {
        double enemyGoonRadius = 2.5;
        if (unit.friendsNear().dragoons().countInRadius(3.5, unit) >= 1) {
            enemyGoonRadius = 5;
        }

        Selection enemyGoons = unit.enemiesNear().dragoons().inRadius(enemyGoonRadius, unit);
        if (enemyGoons.empty()) return Decision.INDIFFERENT;

        return Decision.TRUE;
    }

    private static Decision whenEarlyDefendingChokeVZ(AUnit unit) {
        if (!Enemy.zerg()) return Decision.INDIFFERENT;
        if (!unit.isMissionDefendOrSparta()) return Decision.INDIFFERENT;
        if (A.supplyUsed() >= 50) return Decision.INDIFFERENT;

        if (unit.nearestChokeDist() <= 3) return Decision.TRUE;
        if (unit.friendsNear().dragoons().countInRadius(3.5, unit) > 0) return Decision.TRUE;

        return Decision.INDIFFERENT;
    }

    private static Decision whenWorkersFightingNearby(AUnit unit) {
        if (A.s <= 60 * 6) return Decision.INDIFFERENT;

        if (unit.friendsNear().workers().attacking().countInRadius(8, unit) >= 2) return Decision.TRUE;

        return Decision.INDIFFERENT;
    }

    private static Decision whenGoonsFightingNearby(AUnit unit) {
        if (unit.hp() <= 25) return Decision.FALSE;
        if (unit.cooldown() >= 7) return Decision.FALSE;

        Selection goons = unit.friendsNear().dragoons().notRunning().inRadius(3, unit);

        if (goons.atLeast(2)) return Decision.TRUE;
        if (goons.atLeast(1) && unit.eval() >= 0.9) return Decision.TRUE;

        return Decision.INDIFFERENT;
    }

    private static boolean earlyZealotAvoidBecauseAlmostDead(AUnit unit) {
        if (!unit.isZealot()) return false;

        if (unit.isMissionDefend()) {
            return unit.hp() <= 25;
        }

        return Count.ourCombatUnits() <= 10 && unit.hp() <= 35;
    }
//    private static boolean earlyZealotAvoidBecauseAlmostDeadOrOverpowered(AUnit unit) {
//        if (!unit.isZealot()) return false;
//        if (!unit.isMissionDefend()) return false;
//
//        return Count.ourCombatUnits() <= 10
//            && (unit.hp() <= 33 || unit.eval() < 0.8)
//            && unit.isTargetRanged()
//            && unit.distToTarget() <= 2
//            && unit.friendsNear().ranged().inRadius(4.5, unit).atLeast(1);
//    }

    private static boolean healthyOrNotAttackedLong(AUnit unit) {
        return unit.shieldDamageAtMost(9) || unit.lastAttackFrameMoreThanAgo(30 * 5);
    }

    private static Decision whenMissionDefendDontAvoid(AUnit unit) {
        if (!unit.isMissionDefendOrSparta()) return Decision.INDIFFERENT;

        if (Enemy.zerg()) {
            if (unit.hp() <= 30 && unit.isMelee()) return Decision.FALSE;
            if (unit.eval() <= 0.7) return Decision.FALSE;
        }

        HasPosition safety = unit.safetyPosition();
        if (safety != null && unit.noCooldown() && safety.distTo(unit) <= 3) {
            return Decision.TRUE;
        }

        int tooManyEnemies = unit.shields() >= 40 ? 3 : 2;
        if (unit.enemiesNear().countInRadius(1.4, unit) >= tooManyEnemies) return Decision.FALSE;

        return Decision.INDIFFERENT;
    }
}
