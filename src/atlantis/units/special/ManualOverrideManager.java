package atlantis.units.special;

import atlantis.architecture.Manager;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ManualOverrideManager extends Manager {
    public ManualOverrideManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isPatrolling() && !A.everyNthGameFrame(40 * 5);
    }

    protected Manager handle() {
        return usedManager(this, "#Manual");
    }
}

