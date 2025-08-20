package atlantis.protoss.shuttle.transports.island_recover;

import atlantis.architecture.Manager;
import atlantis.combat.missions.drops.ProtossShouldDropToIsland;
import atlantis.combat.squad.squads.iota.Iota;
import atlantis.units.AUnit;

public class ShuttleRecoverFromIslands extends Manager {
    public ShuttleRecoverFromIslands(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isShuttle()
            && !Iota.get().isEmpty()
            && !ProtossShouldDropToIsland.check();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossShuttleRecoverFromIslandsLoadUnits.class,
            ProtossShuttleRecoverFromIslandsUnloadUnits.class,
        };
    }

}
