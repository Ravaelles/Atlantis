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
        return unit.isActiveManager(ProtossMoon.class)
            && (unit.isStopped() || unit.lastPositionChangedMoreThanAgo(50));
    }

    @Override
    public Manager handle() {
        if ((new AttackNearbyEnemies(unit)).forceHandle() != null) {
            return usedManager(this);
        }

        if (unit.moveToLeader(Actions.MOVE_FOLLOW)) {
            return usedManager(this);
        }

        return null;
    }
}
