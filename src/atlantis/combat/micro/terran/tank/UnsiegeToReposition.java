package atlantis.combat.micro.terran.tank;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.managers.Manager;

public class UnsiegeToReposition extends Manager {

    public UnsiegeToReposition(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (
            noEnemiesNear()
            && allowUnsiegingToReposition()
        ) {
            unit.setTooltip("Reposition");
            return usingManager(this);
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
