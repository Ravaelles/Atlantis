package atlantis.protoss.shuttle.transports.island_drop;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossShuttleDropToIslandsLoadUnits::new,
            ProtossShuttleDropToIslandsUnloadUnits::new,
        };
    }
}
