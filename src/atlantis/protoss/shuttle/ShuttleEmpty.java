package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.units.AUnit;

public class ShuttleEmpty extends Manager {
    private AUnit target;

    public ShuttleEmpty(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isShuttle()) return false;
        if (!unit.loadedUnits().isEmpty()) return false;

        return true;
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossShuttleAvoidAA::new,
            ProtossShuttleEmptyGoToReaver::new,
            ProtossShuttleEmptyAvoidEnemies::new,
        };
    }
}
