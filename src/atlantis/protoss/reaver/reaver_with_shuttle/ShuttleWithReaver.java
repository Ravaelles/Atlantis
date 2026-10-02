package atlantis.protoss.reaver.reaver_with_shuttle;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.protoss.shuttle.ProtossShuttleAvoidAA;
import atlantis.protoss.shuttle.ProtossShuttleAvoidEnemies;
import atlantis.units.AUnit;

import static atlantis.units.AUnitType.Protoss_Reaver;
import static atlantis.units.AUnitType.Protoss_Shuttle;

public class ShuttleWithReaver extends Manager {
    public ShuttleWithReaver(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.type().is(Protoss_Shuttle) && unit.loadedUnitsGet(Protoss_Reaver) != null;
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossShuttleAvoidAA::new,
            ProtossShuttleWithReaverRun::new,

            ProtossShuttleWithReaverEngage::new,
            ProtossShuttleWithReaverIdle::new,

            ProtossShuttleAvoidEnemies::new,
        };
    }
}
