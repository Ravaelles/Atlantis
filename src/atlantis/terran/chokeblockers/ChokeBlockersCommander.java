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
        ChokeBlockersAssignments chokeBlockers = ChokeBlockersAssignments.get();

        chokeBlockers.removeDeadUnits();
        chokeBlockers.assignWorkersWhenNeeded();
        chokeBlockers.assignZealotsWhenNeeded();

        for (AUnit blocker : chokeBlockers.blockers) {
            actWithWorker(blocker);
        }
    }

    private void actWithWorker(AUnit unit) {
        if (unit != null && unit.isAlive()) {
            (new ChokeBlockerManager(unit)).invoke(this);
        }
    }
}
