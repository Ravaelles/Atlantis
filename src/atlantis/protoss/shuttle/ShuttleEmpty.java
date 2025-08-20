package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossShuttleAvoidAA.class,
            ProtossShuttleEmptyGoToReaver.class,
            ProtossShuttleEmptyAvoidEnemies.class,
        };
    }
}
