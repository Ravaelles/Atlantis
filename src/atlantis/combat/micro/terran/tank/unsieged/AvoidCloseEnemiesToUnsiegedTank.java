package atlantis.combat.micro.terran.tank.unsieged;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AvoidCloseEnemiesToUnsiegedTank extends Manager {
    private Selection enemies;

    public AvoidCloseEnemiesToUnsiegedTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.noCooldown() || unit.hp() >= 135) return false;

        enemies = unit.enemiesNear().groundUnits().canAttack(unit, 1.4 + unit.woundPercent() / 50.0);

        return enemies.atLeast(1);
    }

    protected Manager handle() {
        unit.runningManager().runFrom(
            enemies.nearestTo(unit), 2, Actions.MOVE_AVOID, false
        );
        return usedManager(this);
    }
}
