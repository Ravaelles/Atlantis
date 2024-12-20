package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ProtossShuttle extends Manager {
    private AUnit target;

    public ProtossShuttle(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isShuttle()) return false;

        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossShuttleWithReaver.class,
            ProtossShuttleEmpty.class,
        };
    }
}
