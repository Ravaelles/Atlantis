package atlantis.combat.micro.terran.tank.unsieging;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class UnsiegeToReposition extends Manager {
    public UnsiegeToReposition(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTankSieged()
            && unit.noCooldown()
            && unit.lastActionMoreThanAgo(30 * 6, Actions.SIEGE)
            && noEnemiesNear()
            && allowUnsiegingToReposition();
    }

    protected Manager handle() {
        TerranTank.wantsToUnsiege(unit);
        unit.setTooltip("Reposition");
        return usedManager(this);
    }

    private boolean noEnemiesNear() {
        return unit.enemiesNear().combatUnits().empty()
            && unit.lastAttackFrameMoreThanAgo(30 * 3 + (unit.id() % 3))
            && unit.distToLeader() >= 8;
    }

    private boolean allowUnsiegingToReposition() {
        if (unit.cooldownRemaining() == 0 && (A.now() % (1 + unit.id()) <= 1)) return true;

        return false;
    }
}
