package atlantis.combat.squad.positioning.protoss.far_ahead;

import atlantis.architecture.Manager;
import atlantis.game.player.Enemy;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossTooFarAhead extends Manager {
    public ProtossTooFarAhead(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isMissionSparta()) return false;
        if (Army.strength() >= 600) return false;
        if (Enemy.zerg() && unit.isMissionDefendOrSparta()) return false;
        if (unit.isRunningOrRetreating()) return false;
        if (unit.cooldown() <= 8) return false;

        AUnit leader = unit.squadLeader();
        if (leader == null) return false;
        if (unit.isLeader()) return false;

        if (unit.eval() >= 4) return false;

//        if (leader.lastAttackFrameLessThanAgo(30 * 6)) return false;
        if (unit.cooldown() <= 8 && unit.squad().lastUnderAttackLessThanAgo(40)) return false;
        if (unit.cooldown() <= 8 && unit.meleeEnemiesNearCount(unit.isRanged() ? 4.7 : 2) > 0) return false;
        if (unit.distToCannon() <= 1.2) return false;

        return leader.nearestEnemyDist() + 0.4 > unit.nearestEnemyDist();
    }

    @Override
    public Manager handle() {
        AUnit enemy = unit.nearestEnemy();
        if (enemy == null || !enemy.canAttackTarget(unit)) return null;

        boolean distToMainOk = unit.distToMain() >= 16;

        if (distToMainOk && unit.moveToMain(Actions.MOVE_FORMATION)) {
            return usedManager(this);
        }

        if (unit.moveAwayFrom(enemy, moveDistance(), Actions.MOVE_FORMATION)) {
            return usedManager(this);
        }

        if (unit.runningManager().runFrom(enemy, 6, Actions.RUN_ENEMY, unit.hp() <= 65)) {
            return usedManager(this);
        }

        return null;
    }

    private int moveDistance() {
        if (unit.distToLeader() <= 4) {
            return 1;
        }

        return 3;
    }
}
