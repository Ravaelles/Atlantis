package atlantis.protoss.reaver.reaver_with_shuttle;

import atlantis.architecture.Manager;
import atlantis.protoss.shuttle.ProtossShuttleAvoidAA;
import atlantis.protoss.shuttle.ProtossShuttleAvoidEnemies;
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
