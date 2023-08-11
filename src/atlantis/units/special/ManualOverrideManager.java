package atlantis.units.special;

import atlantis.architecture.Manager;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ManualOverrideManager extends Manager {
    public ManualOverrideManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isPatrolling() && unit.lastActionLessThanAgo(30 * 4, Actions.PATROL);
    }

    protected Manager handle() {
        return usedManager(this, "#Manual");
    }
}

