package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class UnsiegeToReposition extends Manager {
    public UnsiegeToReposition(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTankSieged();
    }

    public Manager handle() {
        if (
            noEnemiesNear()
            && allowUnsiegingToReposition()
        ) {
            unit.setTooltip("Reposition");
            return usedManager(this);
        }

        return null;
    }

    private boolean noEnemiesNear() {
        return unit.enemiesNear().combatUnits().empty()
            && unit.lastAttackFrameMoreThanAgo(30 * 3 + (unit.id() % 3))
            && unit.distToLeader() >= 8;
    }

    private boolean allowUnsiegingToReposition() {
        if (unit.cooldownRemaining() == 0 && (A.now() % (1 + unit.id()) <= 1)) {
            return true;
        }

        return false;
    }
}
