package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class RunCauseEnemiesAreTooCloseToUnsiegedTank extends Manager {
    private Selection enemies;

    public RunCauseEnemiesAreTooCloseToUnsiegedTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.noCooldown() || unit.hp() >= 130) return false;

        enemies = unit.enemiesNear().groundUnits().combatUnits().inRadius(6.5, unit);

        return enemies.atLeast(1);
    }

    public Manager handle() {
        unit.runningManager().runFrom(
            enemies.nearestTo(unit), 2, Actions.MOVE_AVOID, false
        );
        return usedManager(this);
    }
}
