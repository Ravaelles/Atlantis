package atlantis.combat.squad.positioning.protoss.far_ahead;

import atlantis.architecture.Manager;
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

        AUnit leader = unit.squadLeader();
        if (leader == null) return false;
        if (leader.lastAttackFrameLessThanAgo(30 * 6)) return false;
        if (unit.cooldown() <= 8 && unit.meleeEnemiesNearCount(unit.isRanged() ? 3.8 : 2) > 0) return false;

        return leader.nearestEnemyDist() + 0.4 > unit.nearestEnemyDist();
    }

    @Override
    public Manager handle() {
        AUnit enemy = unit.nearestEnemy();
        if (enemy == null || !enemy.canAttackTarget(unit)) return null;

        if (unit.moveAwayFrom(enemy, moveDistance(), Actions.MOVE_FORMATION)) {
            return usedManager(this);
        }

        if (unit.moveToMain(Actions.MOVE_FORMATION)) {
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
