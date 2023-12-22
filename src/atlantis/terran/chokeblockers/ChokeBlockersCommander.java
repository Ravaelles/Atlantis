package atlantis.terran.chokeblockers;

import atlantis.architecture.Commander;
import atlantis.units.AUnit;

public class ChokeBlockersCommander extends Commander {
    @Override
    public boolean applies() {
        return NeedChokeBlockers.check();
    }

    @Override
    protected void handle() {
        ChokeBlockers chokeBlockers = ChokeBlockers.get();

        chokeBlockers.assignWorkersIfNeeded();

        actWithWorker(chokeBlockers.worker1);
        actWithWorker(chokeBlockers.worker2);
    }

    private void actWithWorker(AUnit unit) {
        if (unit != null && unit.isAlive()) {
            (new ChokeBlockerManager(unit)).invoke(this);
        }
    }
}
