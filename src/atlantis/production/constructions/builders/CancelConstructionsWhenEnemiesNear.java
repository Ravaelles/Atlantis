package atlantis.production.constructions.builders;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class CancelConstructionsWhenEnemiesNear extends Manager {
    public CancelConstructionsWhenEnemiesNear(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    @Override
    public Manager handle() {
        if (unit.isMoving()) {
            CancelConstructionsWhereEnemyIsTooClose.cancelIfNeeded(unit, unit.construction());
        }

        return null;
    }
}
