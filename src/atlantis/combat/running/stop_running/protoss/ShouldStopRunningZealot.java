package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ShouldStopRunningZealot extends Manager {
    public ShouldStopRunningZealot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isZealot()
            && (unit.cooldown() <= 4 && (unit.distToCannon() <= 1.8 || unit.distToBase() <= 5));
    }

    @Override
    public Manager handle() {
        if (ProtossShouldStopRunning.decisionStopRunning(unit)) {
            return usedManager(this);
        }

        return null;
    }
}

