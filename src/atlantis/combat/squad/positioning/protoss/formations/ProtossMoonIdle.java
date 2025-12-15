package atlantis.combat.squad.positioning.protoss.formations;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossMoonIdle extends Manager {
    public ProtossMoonIdle(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

        return unit.isActiveManager(ProtossMoon.class)
            && (unit.isStopped() || unit.lastPositionChangedMoreThanAgo(100));
    }

    @Override
    public Manager handle() {
        if ((new AttackNearbyEnemies(unit)).invokedFrom(this)) {
            return usedManager(this);
        }

        if (unit.moveToLeader(Actions.MOVE_FOLLOW)) {
            return usedManager(this);
        }

        return null;
    }
}
