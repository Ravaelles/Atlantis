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

        chokeBlockers.assignWorkersWhenNeeded();
        chokeBlockers.assignZealotsWhenNeeded();

        actWithWorker(chokeBlockers.unit1);
        actWithWorker(chokeBlockers.unit2);
    }

    private void actWithWorker(AUnit unit) {
        if (unit != null && unit.isAlive()) {
            (new ChokeBlockerManager(unit)).invoke(this);
        }
    }
}
