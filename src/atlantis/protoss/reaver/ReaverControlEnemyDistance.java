package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ReaverControlEnemyDistance extends Manager {
    public ReaverControlEnemyDistance(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().notEmpty();
    }

    @Override
    public Manager handle() {
        AUnit nearestEnemy = unit.enemiesNear().groundUnits().havingWeapon().nearestTo(unit);
        if (nearestEnemy == null) return null;

        if (unit.runningManager().runFrom(nearestEnemy, 1.5, Actions.RUN_ENEMY, true)) {
            return usedManager(this);
        }

        return null;
    }
}
