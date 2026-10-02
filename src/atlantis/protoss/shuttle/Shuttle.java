package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.protoss.reaver.reaver_with_shuttle.ShuttleWithReaver;
import atlantis.protoss.shuttle.transports.island_drop.ShuttleDropToIslands;
import atlantis.protoss.shuttle.transports.island_recover.ShuttleRecoverFromIslands;
import atlantis.units.AUnit;

public class Shuttle extends Manager {
    private AUnit target;

    public Shuttle(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isShuttle()) return false;

        return true;
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ShuttleWithReaver::new,
            ShuttleEmpty::new,

            ShuttleDropToIslands::new,
            ShuttleRecoverFromIslands::new,
            ShuttleWithReaver::new,
            ShuttleEmpty::new,
        };
    }
}
