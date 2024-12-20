package atlantis.combat.micro.terran.tank.unsieging;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.Enemy;

public class DontThinkAboutUnsieging extends Manager {
    public DontThinkAboutUnsieging(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isTankSieged()) return false;
        if (Enemy.terran()) return false;

        AUnit enemyBreachedBase = EnemyUnitBreachedBase.get();

        if (enemyBreachedBase != null && enemyBreachedBase.distTo(unit) >= 10.5) return false;

        return unit.hp() >= 60 && unit.distToLessThan(unit.squadLeader(), 8);
    }

    @Override
    protected Manager handle() {
        if (
            unit.hasCooldown()
                || unit.lastActionLessThanAgo(30 * (2 + unit.id() % 6), Actions.SIEGE)
                || unit.lastAttackFrameLessThanAgo(30 * (2 + unit.id() % 4))
        ) {
            return usedManager(this);
        }

        return null;
    }
}
