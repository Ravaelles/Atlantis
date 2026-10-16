package atlantis.combat.running.stop_running.zerg;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ShouldStopRunningZergling extends Manager {
    public ShouldStopRunningZergling(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isZergling()
            && unit.meleeEnemiesNearCount(2.1) == 0;
    }

    @Override
    protected Manager handle() {
        return usedManager(this);
    }
}
