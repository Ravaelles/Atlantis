package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.protoss.reaver.reaver_with_shuttle.ShuttleWithReaver;
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

            // BATTLE CUTOVER: island transports (deleted) - mainland only.
            ShuttleWithReaver::new,
            ShuttleEmpty::new,
        };
    }
}
