package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ProtossShouldStopRetreat extends Manager {
    public ProtossShouldStopRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isRetreating()
            && (unit.lastStartedRunningMoreThanAgo(30 * 4) || unit.combatEvalRelative() >= 1.7);
    }

    @Override
    public Manager handle() {
        unit.runningManager().stopRunning();
        return null;
    }
}
