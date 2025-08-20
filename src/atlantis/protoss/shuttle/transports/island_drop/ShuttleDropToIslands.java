package atlantis.protoss.shuttle.transports.island_drop;

import atlantis.architecture.Manager;
import atlantis.combat.missions.drops.ProtossShouldDropToIsland;
import atlantis.units.AUnit;

public class ShuttleDropToIslands extends Manager {
    public ShuttleDropToIslands(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isShuttle()
            && ProtossShouldDropToIsland.check();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossShuttleDropToIslandsLoadUnits.class,
            ProtossShuttleDropToIslandsUnloadUnits.class,
        };
    }
}
