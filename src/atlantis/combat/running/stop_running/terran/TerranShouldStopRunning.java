package atlantis.combat.running.stop_running.terran;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranShouldStopRunning extends Manager {
    public TerranShouldStopRunning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TerranShouldStopRetreat::new,
            ShouldStopRunningMarine::new,
        };
    }
}
