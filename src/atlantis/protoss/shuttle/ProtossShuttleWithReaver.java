package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

import static atlantis.units.AUnitType.Protoss_Reaver;
import static atlantis.units.AUnitType.Protoss_Shuttle;

public class ProtossShuttleWithReaver extends Manager {
    public ProtossShuttleWithReaver(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.type().is(Protoss_Shuttle) && unit.loadedUnitsGet(Protoss_Reaver) != null;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossShuttleAvoidAA.class,
            ProtossShuttleWithReaverRun.class,

            ProtossShuttleWithReaverEngage.class,

            ProtossShuttleAvoidEnemies.class,
        };
    }
}
