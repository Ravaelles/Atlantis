package atlantis.units.special;

import atlantis.architecture.Manager;

import atlantis.units.AUnit;

public class ManualOverrideManager extends Manager {
    public ManualOverrideManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isPatrolling()
            && unit.lastPositionChangedLessThanAgo(60);
    }

    protected Manager handle() {
        return usedManager(this, "#Manual");
    }
}

