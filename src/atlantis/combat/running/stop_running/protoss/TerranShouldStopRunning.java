package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.running.stop_running.terran.ShouldStopRunningMarine;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranShouldStopRunning extends Manager {

    public TerranShouldStopRunning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran()
            && unit.isRunning();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ShouldStopRunningMarine.class,
        };
    }
}
