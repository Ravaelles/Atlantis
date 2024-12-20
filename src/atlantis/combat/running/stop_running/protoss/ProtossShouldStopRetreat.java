package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ProtossShouldStopRetreat extends Manager {
    public ProtossShouldStopRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isRetreating()) return false;
        if (unit.lastStartedRunningLessThanAgo(20)) return false;

        return unit.lastStartedRunningMoreThanAgo(30 * 4)
            || unit.combatEvalRelative() >= 1.4
            || (unit.cooldown() <= 7 && (unit.distToCannon() <= 1.8 || unit.distToBase() <= 5));
    }

    @Override
    public Manager handle() {
        unit.runningManager().stopRunning();
        return null;
    }
}
